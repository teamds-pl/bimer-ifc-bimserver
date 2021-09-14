package pl.ds.bimserver.model.converter;

import org.bimserver.models.ifc4.Ifc4Factory;
import org.bimserver.models.ifc4.IfcConversionBasedUnit;
import org.bimserver.models.ifc4.IfcMeasureWithUnit;
import org.bimserver.models.ifc4.IfcSIPrefix;
import org.bimserver.models.ifc4.IfcSIUnit;
import org.bimserver.models.ifc4.IfcSIUnitName;
import org.bimserver.models.ifc4.IfcUnit;
import org.bimserver.models.ifc4.IfcUnitAssignment;
import org.bimserver.models.ifc4.IfcUnitEnum;
import org.bimserver.models.ifc4.impl.Ifc4FactoryImpl;
import org.eclipse.emf.common.util.EList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Ifc4LengthUnitConverterTest {

    private static final Object UNUSED = null;
    private static final IfcSIPrefix NO_PREFIX = null;
    private static final Ifc4Factory FACTORY = Ifc4FactoryImpl.init();

    private Ifc4LengthUnitConverter converter;

    @Before
    public void setUp() {
        converter = new Ifc4LengthUnitConverter();
    }

    @Test
    public void shouldReturnNullWhenSourceObjectIsNull() {
        Object result = converter.convert(UNUSED, null, String.class, IfcUnitAssignment.class);
        assertNull(result);
    }

    @Test
    public void shouldReturnSiLengthUnitWithoutPrefix() {
        IfcUnitAssignment unitAssignment = FACTORY.createIfcUnitAssignment();
        EList<IfcUnit> units = unitAssignment.getUnits();
        units.add(createSiUnit(IfcUnitEnum.LENGTHUNIT, NO_PREFIX, IfcSIUnitName.METRE));
        units.add(createSiUnit(IfcUnitEnum.MASSUNIT, IfcSIPrefix.KILO, IfcSIUnitName.GRAM));

        Object result = converter.convert(UNUSED, unitAssignment, String.class, IfcUnitAssignment.class);

        assertEquals("METRE", result);
    }

    @Test
    public void shouldReturnSiLengthUnitWithPrefix() {
        IfcUnitAssignment unitAssignment = FACTORY.createIfcUnitAssignment();
        EList<IfcUnit> units = unitAssignment.getUnits();
        units.add(createSiUnit(IfcUnitEnum.MASSUNIT, IfcSIPrefix.KILO, IfcSIUnitName.GRAM));
        units.add(createSiUnit(IfcUnitEnum.LENGTHUNIT, IfcSIPrefix.CENTI, IfcSIUnitName.METRE));

        Object result = converter.convert(UNUSED, unitAssignment, String.class, IfcUnitAssignment.class);

        assertEquals("CENTIMETRE", result);
    }

    @Test
    public void shouldReturnConversionBasedLengthUnit() {
        IfcUnitAssignment unitAssignment = FACTORY.createIfcUnitAssignment();
        EList<IfcUnit> units = unitAssignment.getUnits();
        IfcSIUnit metreUnit = createSiUnit(IfcUnitEnum.LENGTHUNIT, IfcSIPrefix.CENTI, IfcSIUnitName.METRE);

        IfcConversionBasedUnit inchUnit = FACTORY.createIfcConversionBasedUnit();
        inchUnit.setUnitType(IfcUnitEnum.LENGTHUNIT);
        inchUnit.setName("INCH");
        IfcMeasureWithUnit ifcMeasureWithUnit = FACTORY.createIfcMeasureWithUnit();
        ifcMeasureWithUnit.setUnitComponent(metreUnit);
        inchUnit.setConversionFactor(ifcMeasureWithUnit);
        units.add(inchUnit);

        Object result = converter.convert(UNUSED, unitAssignment, String.class, IfcUnitAssignment.class);

        assertEquals("INCH", result);
    }

    private IfcSIUnit createSiUnit(IfcUnitEnum type, IfcSIPrefix prefix, IfcSIUnitName name) {
        IfcSIUnit siUnit = FACTORY.createIfcSIUnit();
        siUnit.setUnitType(type);
        siUnit.setPrefix(prefix);
        siUnit.setName(name);
        return siUnit;
    }

}
