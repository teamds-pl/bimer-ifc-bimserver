package pl.ds.bimserver;

import pl.ds.bimer.ifc.data.model.IfcModel;

import java.io.File;

public interface BimServerIfcParser {

    IfcModel parseIfc2x3tc1(File file) throws BimServerApiException;

    IfcModel parseIfc4(File file) throws BimServerApiException;

}
