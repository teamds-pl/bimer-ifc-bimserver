package pl.ds.bimserver.launcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static pl.ds.bimserver.launcher.TestUtils.getFileFromResources;

@RunWith(MockitoJUnitRunner.class)
public class IfcConverterTest {

    @Mock
    private BimServerIfcParser parser;

    @Mock
    private IfcModel model;

    private IfcConverter converter;

    @Before
    public void setup() {
        converter = new IfcConverter(parser);
    }

    @Test
    public void shouldReturnTrueForFileWithIfcExtension() {
        File file = getFileFromResources("test.ifc");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnTrueForFileWithZipExtensionContainingIfcFile() {
        File file = getFileFromResources("containsIfc.zip");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnTrueForFileWithIfcZipExtensionContainingIfcFile() {
        File file = getFileFromResources("containsIfc.ifczip");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnFalseForFileWithIfcZipExtensionWithoutIfcFile() {
        File file = getFileFromResources("empty.ifczip");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test
    public void shouldReturnFalseForFileWithZipExtensionWithoutIfcFile() {
        File file = getFileFromResources("empty.zip");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test(expected = BimServerApiException.class)
    public void shouldThrowErrorWhenVersionIsNotSupported() throws BimServerApiException {
        File file = getFileFromResources("notSupportedVersion.ifc");
        converter.convert(file);
    }

    @Test
    public void shouldReturnFalseForFileWithoutIfcExtension() {
        File file = getFileFromResources("file");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3Version() throws BimServerApiException {
        File ifc2X3File = getFileFromResources("ifc2X3.ifc");
        when(parser.parseIfc2x3tc1(ifc2X3File)).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3File);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3VersionZip() throws BimServerApiException {
        File ifc2X3ZipFile = getFileFromResources("ifc2X3.zip");
        when(parser.parseIfc2x3tc1(any())).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3ZipFile);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3VersionIfcZip() throws BimServerApiException {
        File ifc2X3IfcZipFile = getFileFromResources("ifc2X3.ifczip");
        when(parser.parseIfc2x3tc1(any())).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3IfcZipFile);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc4Version() throws BimServerApiException {
        File ifc4File = getFileFromResources("ifc4.ifc");
        when(parser.parseIfc4(ifc4File)).thenReturn(model);
        IfcModel result = converter.convert(ifc4File);
        Assert.assertNotNull(result);
    }
}