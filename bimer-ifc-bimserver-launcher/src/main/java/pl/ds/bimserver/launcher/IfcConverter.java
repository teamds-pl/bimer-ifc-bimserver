package pl.ds.bimserver.launcher;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

public class IfcConverter {

    private static final Logger LOG = LoggerFactory.getLogger(IfcConverter.class);

    private final BimServerIfcParser ifcParser;

    public IfcConverter(BimServerIfcParser bimServerIfcParser) {
        this.ifcParser = bimServerIfcParser;
    }

    public boolean canProcess(File file) {
        final String fileName = file.getName();
        LOG.info("Checking if the file: {} can be processed", fileName);
        return endsWithIgnoreCase(fileName, ".ifc") || (canContainIfc(fileName) && isIfcZip(file));
    }

    private static boolean canContainIfc(final String fileName) {
        return endsWithIgnoreCase(fileName, ".zip") || endsWithIgnoreCase(fileName, ".ifczip");
    }

    private boolean isIfcZip(File file) {

        try (FileInputStream fileInputStream = new FileInputStream(file);
             ZipInputStream zipStream = new ZipInputStream(fileInputStream)) {
            ZipEntry entry;
            do {
                entry = zipStream.getNextEntry();
                if (entry != null && isIfc(entry)) {
                    return true;
                }
            } while (entry != null);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read ZIP stream", e);
        }
        return false;
    }

    public IfcModel convert(File file) throws BimServerApiException {
        try {
            if (isZip(file)) {
                file = unzip(file);
            }
            final IfcVersionValidator versionValidator = new IfcVersionValidator(file);
            versionValidator.getVersion();
            IfcModel model;
            if (versionValidator.isIfc2x3()) {
                model = ifcParser.parseIfc2x3tc1(file);

            } else if (versionValidator.isIfc4()) {
                model = ifcParser.parseIfc4(file);
            } else {
                throw new BimServerApiException("Unsupported IFC version " + versionValidator.getVersion());
            }
            return model;
        } catch (RuntimeException e) {
            throw new BimServerApiException("Exception occurred while converting: " + file.getPath(), e);
        }
    }

    private static File unzip(File inputFile) throws BimServerApiException {
        try (ZipFile zip = new ZipFile(inputFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zip.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (isIfc(zipEntry)) {
                    File outputFile = new File(inputFile.getParent(), zipEntry.getName());
                    try (final InputStream is = zip.getInputStream(zipEntry);
                         final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        IOUtils.copy(is, os);
                        return outputFile;
                    }
                }
            }
        } catch (IOException e) {
            throw new BimServerApiException("Unable to read zip file", e);
        }
        throw new BimServerApiException("Unable to find ZIP entry with the IFC file");
    }

    private static boolean isZip(File inputFile) {
        final String name = inputFile.getName().toLowerCase();
        return inputFile.isFile() && (name.endsWith(".zip") || name.endsWith(".ifczip"));
    }

    private static boolean isIfc(ZipEntry zipEntry) {
        return !zipEntry.isDirectory() && zipEntry.getName().toLowerCase().endsWith(".ifc");
    }

}
