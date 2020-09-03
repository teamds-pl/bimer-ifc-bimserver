package pl.ds.bimserver.test;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TestDataLoader {

    private final List<File> createdFolders = new ArrayList<>();
    private final List<File> createdFiles = new ArrayList<>();

    public File getModelAsFile(String folder, String name) {
        File result = null;
        try {
            String path = folder + "/" + name;
            URL url = TestDataLoader.class.getClassLoader().getResource(path);
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                URL packageUrl = TestDataLoader.class.getClassLoader().getResource(folder);
                URLConnection packageConnection = packageUrl.openConnection();
                if (packageConnection instanceof JarURLConnection) {
                    File tempDir = Files.createTempDir();
                    createdFolders.add(tempDir);
                    result = copyFileToDirectory((JarURLConnection) packageConnection, tempDir, name);
                } else {
                    StringBuilder message = new StringBuilder();
                    message.append("Excepted JarUrlConnection insted of ");
                    message.append(packageConnection.getClass());
                    throw new IllegalStateException(message.toString());
                }
            } else {
                File model = new File(url.toURI());
                result = File.createTempFile("model", model.getName());
                FileUtils.copyFile(model, result);
                createdFiles.add(result);
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Exception while reading model file " + name, e);
        }
        return result;
    }

    private File copyFileToDirectory(JarURLConnection jarConnection, File directory, String name) throws IOException {
        File outputFile = new File(directory, name);
        JarFile jarFile = jarConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry entry = (JarEntry) jarEntries.nextElement();
            if (entry.getName().startsWith(jarConnection.getEntryName())) {
                String fileName = StringUtils.removeStart(entry.getName(), jarConnection.getEntryName());
                if (StringUtils.removeStart(fileName, "/").equals(name)) {
                    try (InputStream is = jarFile.getInputStream(entry);
                            final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        IOUtils.copy(is, os);
                        return outputFile;
                    }
                }
            }
        }
        if (!outputFile.exists()) {
            throw new IllegalArgumentException("Resource " + name + " does not exist");
        }
        return outputFile;
    }

    public void cleanup() throws IOException {
        for (File file : createdFolders) {
            FileUtils.deleteDirectory(file);
        }
        for (File file : createdFiles) {
            file.delete();
        }
    }
}
