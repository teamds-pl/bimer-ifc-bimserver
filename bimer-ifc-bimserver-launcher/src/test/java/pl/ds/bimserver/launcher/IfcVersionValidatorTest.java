package pl.ds.bimserver.launcher;

import org.junit.Assert;
import org.junit.Test;
import pl.ds.bimserver.BimServerApiException;

import java.io.File;

import static pl.ds.bimserver.launcher.TestUtils.getFileFromResources;

public class IfcVersionValidatorTest {

    @Test
    public void shouldEvaluateIFC2X3Version() throws BimServerApiException {
        File ifcFile = getFileFromResources("ifc2X3.ifc");
        IfcVersionValidator ifcVersionValidator = new IfcVersionValidator(ifcFile);
        ifcVersionValidator.getVersion();
        Assert.assertEquals("IFC2X3", ifcVersionValidator.getVersion());
        Assert.assertTrue(ifcVersionValidator.isIfc2x3());
        Assert.assertFalse(ifcVersionValidator.isIfc4());
    }

    @Test
    public void shouldEvaluateIFC4Version() throws BimServerApiException {
        File ifcFile = getFileFromResources("ifc4.ifc");
        IfcVersionValidator ifcVersionValidator = new IfcVersionValidator(ifcFile);
        Assert.assertEquals("IFC4", ifcVersionValidator.getVersion());
        Assert.assertTrue(ifcVersionValidator.isIfc4());
        Assert.assertFalse(ifcVersionValidator.isIfc2x3());
    }

    @Test(expected = BimServerApiException.class)
    public void shouldThrowErrorForFileWithoutSchemaVersion() throws BimServerApiException {
        File ifcFile = getFileFromResources("test.ifc");
        IfcVersionValidator ifcVersionValidator = new IfcVersionValidator(ifcFile);
        ifcVersionValidator.getVersion();
    }

}