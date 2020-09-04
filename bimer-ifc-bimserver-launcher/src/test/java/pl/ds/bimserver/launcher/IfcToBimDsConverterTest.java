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
public class IfcToBimDsConverterTest {

    @Mock
    private BimServerIfcParser parser;

    @Mock
    private IfcModel model;

    private IfcToBimDsConverter converter;

    @Before
    public void setup() {
        converter = new IfcToBimDsConverter(parser);
    }

    @Test
    public void shouldReturnTrueForFileWithIfcExtension() {
        File file = getFileFromResources("ifcToBimDsConverter/fileWithIfcExtension.ifc");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnTrueForFileWithZipExtensionContainingIfcFile() {
        File file = getFileFromResources("ifcToBimDsConverter/containsIfc.zip");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnTrueForFileWithIfcZipExtensionContainingIfcFile() {
        File file = getFileFromResources("ifcToBimDsConverter/containsIfc.ifczip");
        Assert.assertTrue(converter.canProcess(file));
    }

    @Test
    public void shouldReturnFalseForFileWithIfcZipExtensionWithoutIfcFile() {
        File file = getFileFromResources("ifcToBimDsConverter/empty.ifczip");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test
    public void shouldReturnFalseForFileWithZipExtensionWithoutIfcFile() {
        File file = getFileFromResources("ifcToBimDsConverter/empty.zip");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test(expected = BimServerApiException.class)
    public void shouldThrowErrorWhenVersionIsNotSupported() throws BimServerApiException {
        File file = getFileFromResources("ifcToBimDsConverter/notSupportedVersion.ifc");
        converter.convert(file);
    }

    @Test
    public void shouldReturnFalseForFileWithoutIfcExtension() {
        File file = getFileFromResources("ifcToBimDsConverter/fileWithoutExtension");
        Assert.assertFalse(converter.canProcess(file));
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3Version() throws BimServerApiException {
        File ifc2X3File = getFileFromResources("ifcToBimDsConverter/ifc2X3Version.ifc");
        when(parser.parseIfc2x3tc1(ifc2X3File)).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3File);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3VersionZip() throws BimServerApiException {
        File ifc2X3ZipFile = getFileFromResources("ifcToBimDsConverter/ifc2X3Version.zip");
        when(parser.parseIfc2x3tc1(any())).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3ZipFile);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc2X3VersionIfcZip() throws BimServerApiException {
        File ifc2X3IfcZipFile = getFileFromResources("ifcToBimDsConverter/ifc2X3Version.ifczip");
        when(parser.parseIfc2x3tc1(any())).thenReturn(model);
        IfcModel result = converter.convert(ifc2X3IfcZipFile);
        Assert.assertNotNull(result);
    }

    @Test
    public void shouldReturnIfcModelForIfc4Version() throws BimServerApiException {
        File ifc4File = getFileFromResources("ifcToBimDsConverter/ifc4Version.ifc");
        when(parser.parseIfc4(ifc4File)).thenReturn(model);
        IfcModel result = converter.convert(ifc4File);
        Assert.assertNotNull(result);
    }
}