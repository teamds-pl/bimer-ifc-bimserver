package pl.ds.bimserver.model.converter;

import com.github.dozermapper.core.CustomConverter;
import org.apache.commons.lang3.StringUtils;

public class ProductTypeConverter implements CustomConverter {


    @Override
    public Object convert(Object destObj, Object srcObj, Class<?> destClass, Class<?> srcClass) {
        return formatType((Class<?>) srcObj);
    }

    private static String formatType(Class<?> productClass) {
        return StringUtils.removeEnd(
                StringUtils.removeStart(productClass.getSimpleName(), "Ifc"), "Impl");
    }
}
