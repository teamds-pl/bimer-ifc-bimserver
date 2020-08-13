package pl.ds.bimserver.impl;

import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;
import pl.ds.bimserver.Ifc2x3Model;
import pl.ds.bimserver.Ifc4Model;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifc.step.deserializer.IfcStepDeserializer;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.ifcopenshell.IfcOpenShellEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BimServerIfcParserImpl implements BimServerIfcParser {

    private static final Logger LOG = LoggerFactory.getLogger(BimServerIfcParserImpl.class);
    private final GeomServerExecutablePathProvider geomServerPathProvider;
    private final IfcStepDeserializersProvider deserializersProvider;

    BimServerIfcParserImpl(GeomServerExecutablePathProvider geomServerPathProvider, IfcStepDeserializersProvider deserializersProvider) {
        this.geomServerPathProvider = geomServerPathProvider;
        this.deserializersProvider = deserializersProvider;
    }

    @Override
    public Ifc2x3Model parseIfc2x3tc1(File file) throws BimServerApiException {
        try {
            IfcStepDeserializer deserializer = deserializersProvider.getIfc2x3tc1StepDeserializer();
            IfcModelInterface model = deserializer.read(file);
            generateGeometry(file, model, true);
            return new Ifc2x3Model(model.getAllWithSubTypes(org.bimserver.models.ifc2x3tc1.IfcObject.class),
                    model.getAllWithSubTypes(org.bimserver.models.ifc2x3tc1.IfcProduct.class),
                    model.getFirst(org.bimserver.models.ifc2x3tc1.IfcProject.class));
        } catch (DeserializeException ex) {
            throw new BimServerApiException(ex);
        }
    }

    @Override
    public Ifc4Model parseIfc4(File file) throws BimServerApiException {
        try {
            IfcStepDeserializer deserializer = deserializersProvider.getIfc4StepDeserializer();
            IfcModelInterface model = deserializer.read(file);
            generateGeometry(file, model, false);
            return new Ifc4Model(model.getAllWithSubTypes(org.bimserver.models.ifc4.IfcObject.class),
                    model.getAllWithSubTypes(org.bimserver.models.ifc4.IfcProduct.class),
                    model.getFirst(org.bimserver.models.ifc4.IfcProject.class));
        } catch (DeserializeException ex) {
            throw new BimServerApiException(ex);
        }
    }

    private void generateGeometry(File file, IfcModelInterface model, boolean isIfc2x3tc1) {
        try (IfcOpenShellEngine renderEngine = new IfcOpenShellEngine(geomServerPathProvider.getGeomServerExecutablePath())) {
            renderEngine.init();
            LOG.info("Using executable " + geomServerPathProvider.getGeomServerExecutablePath());

            try (FileInputStream fis = new FileInputStream(file)) {
                AbstractInputStreamGeometryGenerator generator;
                if (isIfc2x3tc1) {
                    generator = new Ifc2x3InputStreamGeometryGenerator(model, fis, renderEngine);
                } else {
                    generator = new Ifc4InputStreamGeometryGenerator(model, fis, renderEngine);
                }
                generator.generateForAllElements();
            }
        } catch (IOException | RenderEngineException ex) {
            LOG.error("Exception during geometry extraction", ex);
        }
    }
}
