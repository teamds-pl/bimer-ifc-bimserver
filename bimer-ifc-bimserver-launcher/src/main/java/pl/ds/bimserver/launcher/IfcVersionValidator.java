package pl.ds.bimserver.launcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.bimserver.BimServerApiException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IfcVersionValidator {

    private static final Logger LOG = LoggerFactory.getLogger(IfcVersionValidator.class);
    private static final String UNABLE_TO_DETECT_IFC_VERSION = "Unable to detect IFC version";
    private final File file;
    private String version;

    public IfcVersionValidator(File file) {
        this.file = file;
    }

    private void validate() throws BimServerApiException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line;
            while (version == null && (line = fileReader.readLine()) != null) {
                for (String sline : line.split(";")) {
                    if (sline.contains("FILE_SCHEMA")) {
                        if (sline.contains("'")) {
                            int firstIndex = sline.indexOf('\'') + 1;
                            version = sline.substring(firstIndex, sline.indexOf('\'', firstIndex));
                        } else {
                            int firstIndex = sline.indexOf('"') + 1;
                            version = sline.substring(sline.indexOf('"'), sline.lastIndexOf('"', firstIndex));
                        }
                        LOG.info("Detected: {} version", version);
                        break;
                    }
                }
            }
            if (version == null) {
                throw new BimServerApiException(UNABLE_TO_DETECT_IFC_VERSION);
            }
        } catch (IOException ioe) {
            throw new BimServerApiException(UNABLE_TO_DETECT_IFC_VERSION, ioe);
        }
    }

    public String getVersion() throws BimServerApiException {
        if (version == null) {
            validate();
        }
        return version;
    }

    public boolean isIfc2x3() {
        return StringUtils.startsWithIgnoreCase(version, "IFC2X3");
    }

    public boolean isIfc4() {
        return StringUtils.startsWithIgnoreCase(version, "IFC4");
    }
}
