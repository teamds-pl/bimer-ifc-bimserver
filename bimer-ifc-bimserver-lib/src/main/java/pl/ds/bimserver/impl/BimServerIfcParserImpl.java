package pl.ds.bimserver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.ifcopenshell.IfcOpenShellEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.bimer.ifc.data.model.IfcModel;
import pl.ds.bimer.ifc.data.model.IfcProduct;
import pl.ds.bimer.ifc.data.model.impl.IfcBuildingImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcBuildingStoreyImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcElementImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcModelImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcProductImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcProjectImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcSiteImpl;
import pl.ds.bimer.ifc.data.model.impl.IfcSpatialStructureElementImpl;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;
import pl.ds.bimserver.deserializer.IfcStepDeserializer;

public class BimServerIfcParserImpl implements BimServerIfcParser {

    private static final Logger LOG = LoggerFactory.getLogger(BimServerIfcParserImpl.class);
    private final GeomServerExecutablePathProvider geomServerPathProvider;
    private final IfcStepDeserializersProvider deserializersProvider;
    private final Mapper mapper;

    BimServerIfcParserImpl(GeomServerExecutablePathProvider geomServerPathProvider, IfcStepDeserializersProvider deserializersProvider) {
        this.geomServerPathProvider = geomServerPathProvider;
        this.deserializersProvider = deserializersProvider;
        this.mapper = setupMapper();
    }

    private Mapper setupMapper() {
        return DozerBeanMapperBuilder.create()
                .withMappingFiles("mappingsIfc2x3.xml", "mappingsIfc4.xml")
                .build();
    }

    @Override
    public IfcModel parseIfc2x3tc1(File file) throws BimServerApiException {
        try {
            IfcStepDeserializer deserializer = deserializersProvider.getIfc2x3tc1StepDeserializer();
            IfcModelInterface model = deserializer.read(file);
            generateGeometry(file, model, true);
            return generateIfcModel(model, IfcProject.class,
                    org.bimserver.models.ifc2x3tc1.IfcProduct.class);
        } catch (DeserializeException ex) {
            throw new BimServerApiException(ex);
        }
    }

    @Override
    public IfcModel parseIfc4(File file) throws BimServerApiException {
        try {
            IfcStepDeserializer deserializer = deserializersProvider.getIfc4StepDeserializer();
            IfcModelInterface model = deserializer.read(file);
            generateGeometry(file, model, false);
            return generateIfcModel(model, org.bimserver.models.ifc4.IfcProject.class,
                    org.bimserver.models.ifc4.IfcProduct.class);
        } catch (DeserializeException ex) {
            throw new BimServerApiException(ex);
        }
    }

    private IfcModel generateIfcModel(IfcModelInterface model, Class<? extends IdEObject> projectClass,
            Class<? extends IdEObject> productClass) {
        IfcModelImpl dataModel = new IfcModelImpl();
        List<IfcProduct> products = model.getAllWithSubTypes(productClass)
                .stream().map(p -> mapper.map(p, getDestinationClass(p.getClass()))).collect(Collectors.toList());
        dataModel.setProducts(products);
        IfcProjectImpl ifcProject = mapper.map(model.getFirst(projectClass), IfcProjectImpl.class);
        dataModel.setProject(ifcProject);
        return dataModel;
    }

    private Class<? extends IfcProduct> getDestinationClass(Class<?> clazz) {
        Class<? extends IfcProduct> destinationClass;
        if (org.bimserver.models.ifc2x3tc1.IfcBuilding.class.isAssignableFrom(clazz)
                || org.bimserver.models.ifc4.IfcBuilding.class.isAssignableFrom(clazz)) {
            destinationClass = IfcBuildingImpl.class;
        } else if (org.bimserver.models.ifc2x3tc1.IfcSite.class.isAssignableFrom(clazz)
                || org.bimserver.models.ifc4.IfcSite.class.isAssignableFrom(clazz)) {
            destinationClass = IfcSiteImpl.class;
        } else if (org.bimserver.models.ifc2x3tc1.IfcBuildingStorey.class.isAssignableFrom(clazz)
                || org.bimserver.models.ifc4.IfcBuildingStorey.class.isAssignableFrom(clazz)) {
            destinationClass = IfcBuildingStoreyImpl.class;
        } else if (org.bimserver.models.ifc2x3tc1.IfcSpatialStructureElement.class.isAssignableFrom(clazz)
                || org.bimserver.models.ifc4.IfcSpatialStructureElement.class.isAssignableFrom(clazz)) {
            destinationClass = IfcSpatialStructureElementImpl.class;
        } else if (org.bimserver.models.ifc2x3tc1.IfcElement.class.isAssignableFrom(clazz)
                || org.bimserver.models.ifc4.IfcElement.class.isAssignableFrom(clazz)) {
            destinationClass = IfcElementImpl.class;
        } else {
            destinationClass = IfcProductImpl.class;
        }
        return destinationClass;
    }

    private void generateGeometry(File file, IfcModelInterface model, boolean isIfc2x3tc1) {
        try (IfcOpenShellEngine renderEngine = new IfcOpenShellEngine(Paths.get(geomServerPathProvider.getGeomServerExecutablePath()),
                true, true)) {
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
