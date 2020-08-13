package pl.ds.bimserver;

import java.io.File;

public interface BimServerIfcParser {

    Ifc2x3Model parseIfc2x3tc1(File file) throws BimServerApiException;

    Ifc4Model parseIfc4(File file) throws BimServerApiException;

}
