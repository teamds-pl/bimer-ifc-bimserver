package pl.ds.bimserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimer.ifc.data.model.IfcObjectDefinition;
import pl.ds.bimer.ifc.data.model.IfcProject;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;
import pl.ds.bimserver.test.TestDataLoader;
import pl.ds.bimserver.test.TestUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BimServerIfcParserImplTest {

    private static final TestDataLoader LOADER = new TestDataLoader();
    private static BimServerIfcParser ifcParser;

    @BeforeClass
    public static void setup() throws BimServerApiException {
        BimServerClient client = new BimServerClient();
        ifcParser = client.start(getHomePath());
    }

    @AfterClass
    public static void cleanup() throws IOException {
        LOADER.cleanup();
    }

    @Test
    public void shouldReturnNotNull() {
        assertNotNull(model("doors.ifc"));
    }

    @Test
    public void shouldContainProject() {
        assertNotNull(model("doors.ifc").getProject());
    }

    @Test
    public void shouldContainEightIfcProducts() {
        assertEquals(9, model("doors.ifc").getProducts().size());
    }

    @Test
    public void shouldContainProjectDecomposedByBuilding() {
        IfcProject project = model("doors.ifc").getProject();
        String firstDecomposingObjectType = project.getIsDecomposedBy().stream()
                .findFirst()
                .flatMap(ifcRelation -> ifcRelation.getRelatedObjects().stream().findFirst().map(IfcObjectDefinition::getType))
                .orElse(null);
        assertEquals("Building", firstDecomposingObjectType);
    }

    @Test
    public void shouldReturnDoorsIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("doors.ifc"));
        byte[] expectedSerializedData = deserialize("doors.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnDuplexIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("Duplex_A_20110907_optimized.ifc"));
        byte[] expectedSerializedData = deserialize("Duplex_A_20110907_optimized.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnWallOnlyIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("WallOnly.ifc"));
        byte[] expectedSerializedData = deserialize("WallOnly.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnStairGeometryIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("stairGeometry.ifc"));
        byte[] expectedSerializedData = deserialize("stairGeometry.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnWallIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("wall.ifc"));
        byte[] expectedSerializedData = deserialize("wall.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnWallILayersIfcModel() throws IOException {
        byte[] actualSerializedData = serialize(model("wallLayers.ifc"));
        byte[] expectedSerializedData = deserialize("wallLayers.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    @Test
    public void shouldReturnIfc4ModelWithNestedLists() throws IOException {
        // this model is copied from:
        // https://standards.buildingsmart.org/IFC/RELEASE/IFC4/ADD2_TC1/HTML/annex/annex-e/slab-standard-case.ifc
        byte[] actualSerializedData = serialize(model4("ifc4-nested-list.ifc"));
        byte[] expectedSerializedData = deserialize("ifc4-nested-list.data");
        assertArrayEquals(expectedSerializedData, actualSerializedData);
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    private byte[] deserialize(String fileName) throws IOException {
        File file = TestUtils.getFileFromResources("bimServerIfcParserImplTest/" + fileName);
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

    private static IfcModel model4(String name) {
        try {
            File file = LOADER.getModelAsFile("bimServerIfcParserImplTest", name);
            return ifcParser.parseIfc4(file);
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
