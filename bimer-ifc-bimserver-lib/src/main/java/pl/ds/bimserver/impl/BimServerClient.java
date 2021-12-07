package pl.ds.bimserver.impl;

import org.bimserver.emf.MetaDataManager;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.bimserver.BimServerApiException;
import pl.ds.bimserver.BimServerIfcParser;

import java.io.IOException;
import java.nio.file.Files;

public class BimServerClient {

    private static final Logger LOG = LoggerFactory.getLogger(BimServerClient.class);

    public BimServerIfcParser start(String homePath) throws BimServerApiException {
        LOG.info("Initializing MetaDataManager service");
        try {
            MetaDataManager metaDataManager = new MetaDataManager(Files.createTempDirectory("org.bimserver.emf.MetaDataManager"));
            metaDataManager.init(true);
            return initBimServerParser(homePath, metaDataManager);
        } catch (IOException | RenderEngineException e) {
            throw new BimServerApiException("Could not start Bim Server", e);
        }
    }

    private BimServerIfcParser initBimServerParser(String homePath, MetaDataManager metaDataManager) throws RenderEngineException {
        LOG.info("Initializing IfcStepDeserializersProvider service");
        IfcStepDeserializersProvider deserializersProvider = new IfcStepDeserializersProviderImpl(metaDataManager);

        LOG.info("Initializing IfcGeomServerClient");
        try (DsIfcGeomServerClient geomServerClient = new DsIfcGeomServerClient(homePath)) {
            LOG.info("IfcGeomServerClient executables available: {}", geomServerClient.getExecutableFilename());
            LOG.info("Initializing GeomServerExecutablePathProvider service");
            GeomServerExecutablePathProvider geomServerPathProvider = new GeomServerExecutablePathProviderImpl(geomServerClient.getExecutableFilename());

            LOG.info("Initializing BimServerIfcParser service");
            return new BimServerIfcParserImpl(geomServerPathProvider, deserializersProvider);
        }
    }

}
