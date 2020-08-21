package pl.ds.bimserver.model.converter;

import com.github.dozermapper.core.CustomConverter;
import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;

public class OpeningElementConverter implements CustomConverter {


    @Override
    public Object convert(Object destObj, Object srcObj, Class<?> destClass, Class<?> srcClass) {
        return isOpeningElement((Class<?>) srcObj);
    }

    private boolean isOpeningElement(Class<?> productClass) {
        return IfcOpeningElement.class.isAssignableFrom(productClass)
                || org.bimserver.models.ifc4.IfcOpeningElement.class.isAssignableFrom(productClass);
    }
}
