package pl.ds.bimserver.launcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;
import pl.ds.bimserver.impl.BimServerClient;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        BimServerClient bimServerClient = new BimServerClient();
        try {
            BimServerIfcParser ifcParser = bimServerClient.start(getHomePath());
            IfcConverter ifcConverter = new IfcConverter(ifcParser);
            File ifcFile = resolveIfcFile(args);
            if (!ifcConverter.canProcess(ifcFile)) {
                throw new BimServerApiException("Provided file is not IFC file");
            }
            IfcModel ifcModel = ifcConverter.convert(ifcFile);
            writeObjectToStdOut(ifcModel);
        } catch (BimServerApiException e) {
            System.err.println(e.getMessage());
            LOG.error("Error", e);
            System.exit(1);
        }
    }

    private static File resolveIfcFile(String[] args) throws BimServerApiException {
        if (args.length < 1 || StringUtils.isBlank(args[0])) {
            throw new BimServerApiException("Please provide IFC file path as first argument");
        }
        String filePath = args[0];
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BimServerApiException("File with provided path does not exist");
        }
        return file;
    }

    private static String getHomePath() {
        ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();
        String path = location.getPath();
        File file = new File(path);
        return file.getParent();
    }

    private static void writeObjectToStdOut(Serializable object) throws BimServerApiException {
        try (ObjectOutputStream oos = new ObjectOutputStream(System.out)) {
            oos.writeObject(object);
        } catch (IOException e) {
            throw new BimServerApiException("Could not write result", e);
        }
    }
}
