package pl.ds.bimserver.impl;

import com.google.common.base.Charsets;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.shared.exceptions.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DsIfcGeomServerClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsIfcGeomServerClient.class);
    private static final String GEOM_SERVER_JSON_PATH = "geomserver/v0.6.0.json";

    private Process process = null;
    private LittleEndianDataInputStream dis = null;
    private LittleEndianDataOutputStream dos = null;

    private String executableFilename;

    public DsIfcGeomServerClient(String homePath) throws RenderEngineException {
        getExecutable(homePath);
    }

    public String getExecutableFilename() {
        return executableFilename;
    }

    @Override
    public void close() throws RenderEngineException {
        terminate();
    }

    private static String getOs() throws PluginException {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return "win";
        } else if (os.contains("osx") || os.contains("os x") || os.contains("darwin") || os.contains("mac")) {
            return "osx";
        } else if (os.contains("linux")) {
            return "linux";
        } else {
            throw new PluginException(String.format("IfcOpenShell is not available on the %s platorm", os));
        }
    }

    private static String getExecutableExtension() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return ".exe";
        } else {
            return "";
        }
    }

    private void getExecutable(String homePath) throws RenderEngineException {
        boolean initialized = false;
        String platform;

        try {
            InputStream geomServerJson = getClass()
                    .getClassLoader().getResourceAsStream(GEOM_SERVER_JSON_PATH);
            JsonArray builds = new Gson().fromJson(new InputStreamReader(geomServerJson), JsonArray.class);

            String os = getOs();

            if ("osx".equals(os)) {
                platform = "macOS 64";
            } else {
                platform = Character.toUpperCase(os.charAt(0)) + os.substring(1) + " " + System.getProperty("sun.arch.data.model");
            }

            for (int i = 0; i < builds.size(); i++) {
                String platformValue = builds.get(i).getAsJsonObject().get("platform").getAsString();
                String productValue = builds.get(i).getAsJsonObject().get("product").getAsString();
                String filename = builds.get(i).getAsJsonObject().get("filename").getAsString();

                if ("IfcGeomServer".equals(productValue) && platform.equals(platformValue)) {
                    String baseName = productValue + getExecutableExtension();
                    File exePath = Paths.get(homePath).resolve(baseName).toFile();

                    if (!exePath.exists()) {
                        extractGeomServer(filename, baseName, exePath);
                    }

                    initialize(exePath.toString());
                    initialized = true;
                    break;
                }
            }
        } catch (JsonSyntaxException | JsonIOException | IOException | PluginException | URISyntaxException e) {
            throw new RenderEngineException(e);
        }
        if (!initialized) {
            throw new RenderEngineException("No IfcGeomServer executable found for platform '" + platform + "'");
        }
    }

    private void extractGeomServer(String fileName, String baseName, File exePath) throws IOException, URISyntaxException {
        File tempZip = extractTempGeomServerZip(fileName);
        extract(new ZipFile(tempZip), baseName, exePath);
        try {
            Files.setPosixFilePermissions(exePath.toPath(), Collections.singleton(PosixFilePermission.OWNER_EXECUTE));
        } catch (IOException | UnsupportedOperationException e) {
            LOGGER.trace("Exception during setting Posix permissions", e);
        }
    }

    private URI getJarURI() throws URISyntaxException {
        ProtectionDomain domain = DsIfcGeomServerClient.class.getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URL url = source.getLocation();
        return url.toURI();
    }

    private File extractTempGeomServerZip(String fileName) throws IOException, URISyntaxException {
        File location = new File(getJarURI());
        File tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()));
        tempFile.deleteOnExit();
        String pathInZip = "geomserver" + File.separator + fileName;
        if (location.isDirectory()) {
            Path zipFile = Paths.get(location.getPath(), pathInZip);
            Files.copy(zipFile, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            try (final ZipFile zipFile = new ZipFile(location)) {
                extract(zipFile, pathInZip, tempFile);
            }
        }
        return tempFile;
    }

    private static void extract(ZipFile zipFile, String fileName, File outputFile) throws IOException {
        ZipEntry entry = zipFile.getEntry(fileName);
        if (entry == null) {
            throw new IOException("Can not find file " + fileName + " in archive " + zipFile.getName());
        }
        try (InputStream zipStream = zipFile.getInputStream(entry);
             OutputStream fileStream = new FileOutputStream(outputFile)) {
            int i;
            byte[] buf = new byte[1024];
            while ((i = zipStream.read(buf)) != -1) {
                fileStream.write(buf, 0, i);
            }
        }
    }

    private void initialize(String executableFilename) throws RenderEngineException {
        try {
            this.executableFilename = executableFilename;

            process = Runtime.getRuntime().exec(executableFilename);
            dos = new LittleEndianDataOutputStream(process.getOutputStream());
            dis = new LittleEndianDataInputStream(process.getInputStream());

            if (dis.readInt() != HELLO) {
                LOGGER.error("Invalid welcome message received");
                terminate();
                return;
            }
            Hello h = new Hello();
            h.read(dis);

            String reportedVersion = h.getString();
            if (!VERSION.equals(reportedVersion)) {
                terminate();
                throw new RenderEngineException(String.format("Version mismatch: Plugin version %s does not match IfcOpenShell version %s", VERSION, reportedVersion));
            }
        } catch (IOException e) {
            throw new RenderEngineException(e);
        }
    }

    private static final int HELLO = 0xff00;
    private static final int IFC_MODEL = HELLO + 1;
    private static final int GET = IFC_MODEL + 1;
    private static final int ENTITY = GET + 1;
    private static final int MORE = ENTITY + 1;
    private static final int NEXT = MORE + 1;
    private static final int BYE = NEXT + 1;
    private static final int GET_LOG = BYE + 1;
    private static final int LOG = GET_LOG + 1;

    private static final String VERSION = "IfcOpenShell-0.6.0b0-0";

    abstract static class Command {

        abstract void read_contents(LittleEndianDataInputStream s) throws IOException;

        abstract void write_contents(LittleEndianDataOutputStream s) throws IOException;

        int iden;
        int len;

        void read(LittleEndianDataInputStream s) throws IOException {
            len = s.readInt();
            read_contents(s);
        }

        void write(LittleEndianDataOutputStream s) throws IOException {
            s.writeInt(iden);
            ByteArrayOutputStream oss = new ByteArrayOutputStream();
            write_contents(new LittleEndianDataOutputStream(oss));

            // Comment Ruben: It seems redundant to send the size twice (when sending a String, LittleEndianness should not change the size I think)
            // Also storing the intermediate results in another buffer can be avoided I think, why not send the original s variable to write_contents?
            s.writeInt(oss.size());
            s.write(oss.toByteArray());
            s.flush();
        }

        Command(int iden) {
            this.iden = iden;
        }

        protected String readString(LittleEndianDataInputStream s) throws IOException {
            int length = s.readInt();
            byte[] b = new byte[length];
            s.readFully(b);
            String str = new String(b);
            while (length++ % 4 != 0) {
                s.read();
            }
            return str;
        }

        protected float[] readFloatArray(LittleEndianDataInputStream s) throws IOException {
            int length = s.readInt() / 4;
            float[] fs = new float[length];
            for (int i = 0; i < length; ++i) {
                fs[i] = s.readFloat();
            }
            return fs;
        }

        protected double[] readDoubleArray(LittleEndianDataInputStream s) throws IOException {
            int length = s.readInt() / 8;
            double[] fs = new double[length];
            for (int i = 0; i < length; ++i) {
                fs[i] = s.readDouble();
            }
            return fs;
        }

        protected int[] readIntArray(LittleEndianDataInputStream s) throws IOException {
            int length = s.readInt() / 4;
            int[] is = new int[length];
            for (int i = 0; i < length; ++i) {
                is[i] = s.readInt();
            }
            return is;
        }

        protected void writeString(LittleEndianDataOutputStream s, String str) throws IOException {
            byte[] b = str.getBytes(Charsets.UTF_8);
            int length = b.length;
            s.writeInt(length);
            s.write(b);
            while (length++ % 4 != 0) {
                s.write(0);
            }
        }

        protected void writeStringBinary(LittleEndianDataOutputStream s, byte[] data) throws IOException {
            int length = data.length;
            s.writeInt(length);
            s.write(data);
            while (len++ % 4 != 0) {
                s.write(0);
            }
        }

        protected void writeStringBinary(LittleEndianDataOutputStream s, InputStream inputStream, int length) throws IOException {
            s.writeInt(length);
            IOUtils.copy(inputStream, s);
            while (length++ % 4 != 0) {
                s.write(0);
            }
        }
    }

    static class Hello extends Command {

        private String string;

        public String getString() {
            return string;
        }

        Hello() {
            super(HELLO);
        }

        @Override
        void read_contents(LittleEndianDataInputStream s) throws IOException {
            string = readString(s);
        }

        @Override
        void write_contents(LittleEndianDataOutputStream s) {
            throw new UnsupportedOperationException();
        }
    }

    static class Bye extends Command {

        Bye() {
            super(BYE);
        }

        @Override
        void read_contents(LittleEndianDataInputStream s) throws IOException {
        }

        @Override
        void write_contents(LittleEndianDataOutputStream s) {
        }
    }

    static class GetLog extends Command {

        GetLog() {
            super(GET_LOG);
        }

        @Override
        void read_contents(LittleEndianDataInputStream s) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        void write_contents(LittleEndianDataOutputStream s) {
        }
    }

    static class Log extends Command {

        private String string;

        Log() {
            super(LOG);
        }

        @Override
        void read_contents(LittleEndianDataInputStream s) throws IOException {
            string = readString(s);
        }

        @Override
        void write_contents(LittleEndianDataOutputStream s) {
            throw new UnsupportedOperationException();
        }

        public String getString() {
            return string;
        }
    }

    private void terminate() throws RenderEngineException {
        if (process == null) {
            return;
        }

        // Try and get the conversion log and say goodbye to the executable
        try {
            GetLog gl = new GetLog();
            gl.write(dos);

            if (dis.readInt() != LOG) {
                LOGGER.error("Invalid command sequence encountered");
                throw new IOException();
            }

            Log lg = new Log();
            lg.read(dis);

            final String log = lg.getString().trim();
            if (log.length() > 0) {
                LOGGER.info("\n" + log);
            }

            Bye b = new Bye();
            b.write(dos);

            if (dis.readInt() != BYE) {
                LOGGER.error("Invalid command sequence encountered");
                throw new IOException();
            }
            b.read(dis);
        } catch (RuntimeException | IOException e) {
            LOGGER.warn("Exception during process termination", e);
        }

        try {
            // Give the executable some time to terminate by itself or kill
            // it after 2 seconds have passed
            for (int n = 0; ; ) {
                try {
                    if (process.exitValue() != 0) {
                        // LOGGER.error(String.format("Exited with non-zero exit code: %d", process.exitValue()));
                        throw new RenderEngineException(String.format("Exited with non-zero exit code: %d", process.exitValue()));
                    }
                    break;
                } catch (IllegalThreadStateException e) {
                    if (n++ == 20) {
                        process.destroy();
                        LOGGER.error("Forcefully terminated IfcOpenShell process");
                        break;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.trace("Exception during invoking Thread.sleep()", e);
                }
            }
        } finally {
            process.destroyForcibly();
        }

        dis = null;
        dos = null;
        process = null;
    }
}
