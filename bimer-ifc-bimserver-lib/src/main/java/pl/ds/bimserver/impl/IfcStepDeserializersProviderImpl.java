package pl.ds.bimserver.impl;

import org.bimserver.emf.MetaDataManager;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.emf.Schema;
import pl.ds.bimserver.deserializer.Ifc2x3tc1StepDeserializer;
import pl.ds.bimserver.deserializer.Ifc4StepDeserializer;
import pl.ds.bimserver.deserializer.IfcStepDeserializer;

class IfcStepDeserializersProviderImpl implements IfcStepDeserializersProvider {

    private final PackageMetaData ifc2x3MetaData;
    private final PackageMetaData ifc4MetaData;

    IfcStepDeserializersProviderImpl(MetaDataManager metaDataManager) {
        ifc2x3MetaData = metaDataManager.getPackageMetaData(Schema.IFC2X3TC1.toString());
        ifc4MetaData = metaDataManager.getPackageMetaData(Schema.IFC4.toString());
    }

    @Override
    public IfcStepDeserializer getIfc2x3tc1StepDeserializer() {
        IfcStepDeserializer deserializer = new Ifc2x3tc1StepDeserializer();
        deserializer.init(ifc2x3MetaData);
        return deserializer;
    }

    @Override
    public IfcStepDeserializer getIfc4StepDeserializer() {
        IfcStepDeserializer deserializer = new Ifc4StepDeserializer(Schema.IFC4);
        deserializer.init(ifc4MetaData);
        return deserializer;
    }

}
