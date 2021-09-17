package pl.ds.bimserver.model.converter;

import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Factory;
import org.bimserver.models.ifc2x3tc1.IfcConversionBasedUnit;
import org.bimserver.models.ifc2x3tc1.IfcMeasureWithUnit;
import org.bimserver.models.ifc2x3tc1.IfcSIPrefix;
import org.bimserver.models.ifc2x3tc1.IfcSIUnit;
import org.bimserver.models.ifc2x3tc1.IfcSIUnitName;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcUnitEnum;
import org.bimserver.models.ifc2x3tc1.impl.Ifc2x3tc1FactoryImpl;
import org.eclipse.emf.common.util.EList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Ifc2x3tc1LengthUnitConverterTest {

    private static final Object UNUSED = null;
    private static final IfcSIPrefix NO_PREFIX = null;
    private static final Ifc2x3tc1Factory FACTORY = Ifc2x3tc1FactoryImpl.init();

    private Ifc2x3tc1LengthUnitConverter converter;

    @Before
    public void setUp() {
        converter = new Ifc2x3tc1LengthUnitConverter();
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
