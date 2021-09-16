package pl.ds.bimserver.test;

import java.io.File;
import java.net.URL;

public final class TestUtils {

    public static File getFileFromResources(String fileName) {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalStateException("Could not find resource: " + fileName);
        }
        return new File(resource.getFile());
    }
}
