package pl.ds.bimserver.impl;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.geometry.Matrix;
import org.bimserver.models.geometry.Bounds;
import org.bimserver.models.geometry.Buffer;
import org.bimserver.models.geometry.GeometryData;
import org.bimserver.models.geometry.GeometryFactory;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.plugins.renderengine.EntityNotFoundException;
import org.bimserver.plugins.renderengine.IndexFormat;
import org.bimserver.plugins.renderengine.Precision;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEngineFilter;
import org.bimserver.plugins.renderengine.RenderEngineGeometry;
import org.bimserver.plugins.renderengine.RenderEngineInstance;
import org.bimserver.plugins.renderengine.RenderEngineModel;
import org.bimserver.plugins.renderengine.RenderEngineSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractInputStreamGeometryGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInputStreamGeometryGenerator.class);
    private final RenderEngine renderEngine;
    private final InputStream in;
    protected final IfcModelInterface model;
    private final Map<Integer, GeometryData> hashes = new ConcurrentHashMap<>();
    private RenderEngineModel renderEngineModel;

    AbstractInputStreamGeometryGenerator(IfcModelInterface model, InputStream in, RenderEngine renderEngine) {
        this.model = model;
        this.renderEngine = renderEngine;
        this.in = in;
    }

    public void generateForAllElements() {
        try {
            renderEngineModel = renderEngine.openModel(in);
            final RenderEngineSettings settings = new RenderEngineSettings();
            settings.setPrecision(Precision.SINGLE);
            settings.setIndexFormat(IndexFormat.AUTO_DETECT);
            settings.setGenerateNormals(true);
            settings.setGenerateTriangles(true);
            settings.setGenerateWireFrame(false);

            final RenderEngineFilter renderEngineFilter = new RenderEngineFilter();

            renderEngineModel.setSettings(settings);
            renderEngineModel.setFilter(renderEngineFilter);

            renderEngineModel.generateGeneralGeometry();

            generateForAllIfcProducts();
        } catch (RenderEngineException ex) {
            LOGGER.error("Exception during geometry generation", ex);
        }
    }

    protected abstract void generateForAllIfcProducts();

    protected GeometryInfo generateGeometry(long expressId) {
        try {
            RenderEngineInstance renderEngineInstance = renderEngineModel.getInstanceFromExpressId(expressId);
            RenderEngineGeometry geometry = renderEngineInstance.generateGeometry();
            if (geometry != null) {
                setLittleEndianOrderForByteBuffers(geometry);
                int nrIndices = geometry.getNrIndices();
                if (nrIndices > 0) {
                    GeometryInfo geometryInfo = GeometryFactory.eINSTANCE.createGeometryInfo();

                    Bounds bounds = GeometryFactory.eINSTANCE.createBounds();

                    bounds.setMin(createVector3f(Double.POSITIVE_INFINITY));
                    bounds.setMax(createVector3f(-Double.POSITIVE_INFINITY));

                    geometryInfo.setBounds(bounds);

                    try {
                        ObjectNode additionalData = renderEngineInstance.getAdditionalData();
                        if (additionalData != null) {
                            geometryInfo.setAdditionalData(additionalData.toString());
                            if (additionalData.has("TOTAL_SURFACE_AREA")) {
                                geometryInfo.setArea(additionalData.get("TOTAL_SURFACE_AREA").asDouble());
                            }
                            if (additionalData.has("TOTAL_SHAPE_VOLUME")) {
                                geometryInfo.setVolume(additionalData.get("TOTAL_SHAPE_VOLUME").asDouble());
                            }
                        }
                    } catch (UnsupportedOperationException e) {
                        LOGGER.trace("Exception during setting area and volume", e);
                    }

                    GeometryData geometryData = GeometryFactory.eINSTANCE.createGeometryData();
                    geometryData.setIndices(createBuffer(geometry.getIndices()));
                    geometryData.setVertices(createBuffer(geometry.getVertices()));
                    geometryData.setNormals(createBuffer(geometry.getNormals()));

                    geometryInfo.setPrimitiveCount(nrIndices / 3);

                    IntBuffer indicesBuffer = geometry.getIndices().asIntBuffer();
                    if (geometry.getMaterialIndices() != null) {
                        IntBuffer materialIndicesBuffer = geometry.getMaterialIndices().asIntBuffer();
                        FloatBuffer materialsBuffer = geometry.getMaterials().asFloatBuffer();
                        int materialIndicesLength = geometry.getNrMaterialIndices();
                        if (materialIndicesLength > 0) {
                            boolean hasMaterial = false;
                            float[] vertexColors = new float[geometry.getNrVertices() / 3 * 4];
                            for (int i = 0; i < materialIndicesLength; ++i) {
                                int c = materialIndicesBuffer.get(i);
                                for (int j = 0; j < 3; ++j) {
                                    int k = indicesBuffer.get(i * 3 + j);
                                    if (c > -1) {
                                        hasMaterial = true;
                                        for (int l = 0; l < 4; ++l) {
                                            vertexColors[4 * k + l] = materialsBuffer.get(4 * c + l);
                                        }
                                    }
                                }
                            }
                            if (hasMaterial) {
                                geometryData.setColorsQuantized(createBuffer(floatArrayToByteArray(vertexColors)));
                            }
                        }
                    }

                    double[] transformationMatrix = new double[16];
                    Matrix.setIdentityM(transformationMatrix, 0);
                    if (renderEngineInstance.getTransformationMatrix() != null) {
                        transformationMatrix = renderEngineInstance.getTransformationMatrix();
                    }

                    DoubleBuffer verticesBuffer = geometry.getVertices().asDoubleBuffer();
                    for (int i = 0; i < nrIndices; i++) {
                        int index = indicesBuffer.get(i) * 3;
                        processExtends(geometryInfo, verticesBuffer, transformationMatrix, index);
                    }

                    geometryInfo.setData(geometryData);

                    setTransformationMatrix(geometryInfo, transformationMatrix);
                    int hash = hash(geometryData);
                    if (hashes.containsKey(hash)) {
                        geometryInfo.setData(hashes.get(hash));
                    } else {
                        hashes.put(hash, geometryData);
                    }

                    return geometryInfo;
                }
            }
        } catch (EntityNotFoundException e) {
            LOGGER.trace("Entity not found", e);
        } catch (RenderEngineException ex) {
            LOGGER.error("Exception during geometry generation", ex);
        }
        return null;
    }

    private void setLittleEndianOrderForByteBuffers(RenderEngineGeometry geometry) {
        // explicit setting little endian for byte buffers, because we get little endian from server,
        // but these buffers are created with big endian order and read values are incorrect
        geometry.getIndices().order(ByteOrder.LITTLE_ENDIAN);
        geometry.getVertices().order(ByteOrder.LITTLE_ENDIAN);
        geometry.getNormals().order(ByteOrder.LITTLE_ENDIAN);
        geometry.getMaterials().order(ByteOrder.LITTLE_ENDIAN);
        geometry.getMaterialIndices().order(ByteOrder.LITTLE_ENDIAN);
    }

    private Buffer createBuffer(byte[] data) {
        Buffer buffer = GeometryFactory.eINSTANCE.createBuffer();
        buffer.setData(data);
        return buffer;
    }

    private Buffer createBuffer(ByteBuffer data) {
        Buffer buffer = GeometryFactory.eINSTANCE.createBuffer();
        buffer.setData(data.array());
        return buffer;
    }

    private byte[] floatArrayToByteArray(float[] values) {
        if (values == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(new byte[values.length * 4]);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer asFloatBuffer = buffer.asFloatBuffer();
        for (float f : values) {
            asFloatBuffer.put(f);
        }
        return buffer.array();
    }

    private void setTransformationMatrix(GeometryInfo geometryInfo, double[] transformationMatrix) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 8);
        byteBuffer.order(ByteOrder.nativeOrder());
        DoubleBuffer asDoubleBuffer = byteBuffer.asDoubleBuffer();
        for (double f : transformationMatrix) {
            asDoubleBuffer.put(f);
        }
        geometryInfo.setTransformation(byteBuffer.array());
    }

    private Vector3f createVector3f(double defaultValue) {
        Vector3f vector3f = GeometryFactory.eINSTANCE.createVector3f();
        vector3f.setX(defaultValue);
        vector3f.setY(defaultValue);
        vector3f.setZ(defaultValue);
        return vector3f;
    }

    private int hash(GeometryData geometryData) {
        int hashCode = 0;
        if (geometryData.getIndices() != null) {
            hashCode += Arrays.hashCode(geometryData.getIndices().getData());
        }
        if (geometryData.getVertices() != null) {
            hashCode += Arrays.hashCode(geometryData.getVertices().getData());
        }
        if (geometryData.getNormals() != null) {
            hashCode += Arrays.hashCode(geometryData.getNormals().getData());
        }
        if (geometryData.getColorsQuantized() != null) {
            hashCode += Arrays.hashCode(geometryData.getColorsQuantized().getData());
        }
        return hashCode;
    }

    private void processExtends(GeometryInfo geometryInfo, DoubleBuffer verticesBuffer, double[] transformationMatrix, int index) {
        double x = verticesBuffer.get(index);
        double y = verticesBuffer.get(index + 1);
        double z = verticesBuffer.get(index + 2);
        double[] result = new double[4];
        Matrix.multiplyMV(result, 0, transformationMatrix, 0, new double[] { x, y, z, 1 }, 0);
        x = result[0];
        y = result[1];
        z = result[2];
        Bounds bounds = geometryInfo.getBounds();
        bounds.getMin().setX(Math.min(x, bounds.getMin().getX()));
        bounds.getMin().setY(Math.min(y, bounds.getMin().getY()));
        bounds.getMin().setZ(Math.min(z, bounds.getMin().getZ()));
        bounds.getMax().setX(Math.max(x, bounds.getMax().getX()));
        bounds.getMax().setY(Math.max(y, bounds.getMax().getY()));
        bounds.getMax().setZ(Math.max(z, bounds.getMax().getZ()));
    }
}
