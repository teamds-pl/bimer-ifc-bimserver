package pl.ds.bimserver.impl;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimer.ifc.data.model.IfcObjectDefinition;
import pl.ds.bimer.ifc.data.model.IfcProject;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;
import pl.ds.bimserver.test.TestDataLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import static pl.ds.bimserver.test.TestUtils.getFileFromResources;

public class BimServerIfcParserImplTest {

    private static final TestDataLoader LOADER = new TestDataLoader();
    private static BimServerIfcParser ifcParser;
    private static IfcModel doorsModel;

    @BeforeClass
    public static void setup() throws BimServerApiException {
        BimServerClient client = new BimServerClient();
        ifcParser = client.start(getHomePath());
        doorsModel = model("doors.ifc");
    }

    @AfterClass
    public static void cleanup() throws IOException {
        LOADER.cleanup();
    }

    @Test
    public void shouldReturnNotNull() {
        Assert.assertNotNull(doorsModel);
    }

    @Test
    public void shouldContainProject() {
        Assert.assertNotNull(doorsModel.getProject());
    }

    @Test
    public void shouldContainEightIfcProducts() {
        Assert.assertEquals(9, doorsModel.getProducts().size());
    }

    @Test
    public void shouldContainProjectDecomposedByBuilding() {
        IfcProject project = doorsModel.getProject();
        String firstDecomposingObjectType = project.getIsDecomposedBy().stream().findFirst().flatMap(ifcRelation -> ifcRelation.getRelatedObjects().stream().findFirst().map(IfcObjectDefinition::getType)).orElse(null);
        Assert.assertEquals("Building", firstDecomposingObjectType);
    }

    @Test
    public void shouldReturnDoorsIfcModel() throws IOException {
        Assert.assertArrayEquals(deserialize("bimServerIfcParserImplTest/doors.data"), serialize(doorsModel));
    }

    @Test
    public void shouldReturnDuplexIfcModel() throws IOException {
        byte[] expecteds = deserialize("bimServerIfcParserImplTest/Duplex_A_20110907_optimized.data");
        byte[] actuals = serialize(model("Duplex_A_20110907_optimized.ifc"));
        Assert.assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void shouldReturnWallOnlyIfcModel() throws IOException {
        byte[] expecteds = deserialize("bimServerIfcParserImplTest/WallOnly.data");
        byte[] actuals = serialize(model("WallOnly.ifc"));
        Assert.assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void shouldReturnStairGeometryIfcModel() throws IOException {
        byte[] expecteds = deserialize("bimServerIfcParserImplTest/stairGeometry.data");
        byte[] actuals = serialize(model("stairGeometry.ifc"));
        Assert.assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void shouldReturnWallIfcModel() throws IOException {
        byte[] expecteds = deserialize("bimServerIfcParserImplTest/wall.data");
        byte[] actuals = serialize(model("wall.ifc"));
        Assert.assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void shouldReturnWallILayersfcModel() throws IOException {
        byte[] expecteds = deserialize("bimServerIfcParserImplTest/wallLayers.data");
        byte[] actuals = serialize(model("wallLayers.ifc"));
        Assert.assertArrayEquals(expecteds, actuals);
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    private byte[] deserialize(String filePath) throws IOException {
        File file = getFileFromResources(filePath);
        FileInputStream stream = new FileInputStream(file);
        return IOUtils.toByteArray(stream);
    }

    private static IfcModel model(String name) {
        try {
            File file = LOADER.getModelAsFile("bimServerIfcParserImplTest", name);
            return ifcParser.parseIfc2x3tc1(file);
        } catch (BimServerApiException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getHomePath() {
        ProtectionDomain protectionDomain = BimServerIfcParserImplTest.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();
        String path = location.getPath();
        File file = new File(path);
        return file.getParent();
    }
}