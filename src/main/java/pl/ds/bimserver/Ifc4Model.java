package pl.ds.bimserver;

import org.bimserver.models.ifc4.IfcObject;
import org.bimserver.models.ifc4.IfcProduct;
import org.bimserver.models.ifc4.IfcProject;

import java.util.List;

public class Ifc4Model {

    private final List<IfcObject> objects;
    private List<IfcProduct> products;
    private IfcProject project;

    public Ifc4Model(List<IfcObject> objects, List<IfcProduct> products, IfcProject project) {
        this.objects = objects;
        this.products = products;
        this.project = project;
    }

    public List<IfcObject> getObjects() {
        return objects;
    }

    public List<IfcProduct> getProducts() {
        return products;
    }

    public IfcProject getProject() {
        return project;
    }
}
