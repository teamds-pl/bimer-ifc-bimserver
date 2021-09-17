package pl.ds.bimserver.model.converter;

import com.github.dozermapper.core.CustomConverter;
import org.bimserver.models.ifc2x3tc1.IfcConversionBasedUnit;
import org.bimserver.models.ifc2x3tc1.IfcSIPrefix;
import org.bimserver.models.ifc2x3tc1.IfcSIUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcUnitEnum;

public class Ifc2x3tc1LengthUnitConverter implements CustomConverter {

    @Override
    public Object convert(Object destObj, Object srcObj, Class<?> destClass, Class<?> srcClass) {
        if (srcObj == null || !IfcUnitAssignment.class.isAssignableFrom(srcClass)) {
            return null;
        }
        return extractLengthUnit((IfcUnitAssignment) srcObj);
    }

    private String extractLengthUnit(IfcUnitAssignment ifcUnitAssignment) {
        for (IfcUnit unit : ifcUnitAssignment.getUnits()) {
            if (unit instanceof IfcSIUnit) {
                IfcSIUnit siUnit = (IfcSIUnit) unit;
                IfcUnitEnum unitType = siUnit.getUnitType();
                if (unitType == IfcUnitEnum.LENGTHUNIT) {
                    IfcSIPrefix prefix = siUnit.getPrefix();
                    String literal = siUnit.getName().getLiteral();
                    return (prefix == null || prefix.getLiteral().equals("NULL")) ? literal : prefix + literal;
                }
            } else if (unit instanceof IfcConversionBasedUnit) {
                IfcConversionBasedUnit conversionBasedUnit = (IfcConversionBasedUnit) unit;
                IfcUnitEnum unitType = conversionBasedUnit.getUnitType();
                if (unitType == IfcUnitEnum.LENGTHUNIT) {
                    return conversionBasedUnit.getName();
                }
            }
        }
        return null;
    }

}
