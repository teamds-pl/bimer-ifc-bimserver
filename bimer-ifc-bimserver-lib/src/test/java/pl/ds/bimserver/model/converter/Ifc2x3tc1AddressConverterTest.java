package pl.ds.bimserver.model.converter;

import org.bimserver.models.ifc2x3tc1.IfcPostalAddress;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Ifc2x3tc1AddressConverterTest {

    private Ifc2x3tc1AddressConverter converter = new Ifc2x3tc1AddressConverter();
    @Mock
    IfcPostalAddress address;

    @Test
    public void shouldReturnEmptyString() {
        Object result = converter.convert("", null, String.class, IfcPostalAddress.class);
        Assert.assertEquals("", result);
    }

    @Test
    public void shouldReturnAddressLinesString() {
        BasicEList<String> addressLines = new BasicEList<>();
        addressLines.add("Line 1");
        addressLines.add("Line 2");
        when(address.getAddressLines()).thenReturn(addressLines);
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Line 1" + System.lineSeparator() + "Line 2", result);
    }

    @Test
    public void shouldReturnInternalLocationString() {
        when(address.getInternalLocation()).thenReturn("Internal location");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Internal location", result);
    }

    @Test
    public void shouldReturnPostalBoxString() {
        when(address.getPostalBox()).thenReturn("Postal Box");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Postal Box", result);
    }

    @Test
    public void shouldReturnTownString() {
        when(address.getTown()).thenReturn("Town");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Town", result);
    }

    @Test
    public void shouldReturnRegionString() {
        when(address.getRegion()).thenReturn("Region");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Region", result);
    }

    @Test
    public void shouldReturnPostalCodeString() {
        when(address.getPostalCode()).thenReturn("Postal Code");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Postal Code", result);
    }

    @Test
    public void shouldReturnCountryString() {
        when(address.getCountry()).thenReturn("Country");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        Assert.assertEquals("Country", result);
    }

    @Test
    public void shouldReturnFullAddressWithLineSeparators() {
        BasicEList<String> addressLines = new BasicEList<>();
        addressLines.add("Line 1");
        addressLines.add("Line 2");
        when(address.getAddressLines()).thenReturn(addressLines);
        when(address.getInternalLocation()).thenReturn("Internal location");
        when(address.getPostalBox()).thenReturn("Postal Box");
        when(address.getTown()).thenReturn("Town");
        when(address.getRegion()).thenReturn("Region");
        when(address.getPostalCode()).thenReturn("Postal Code");
        when(address.getCountry()).thenReturn("Country");
        Object result = converter.convert("", address, String.class, IfcPostalAddress.class);
        String addressBuilder = "Line 1" + System.lineSeparator() + "Line 2" +
                System.lineSeparator() + "Internal location" +
                System.lineSeparator() + "Postal Box" +
                System.lineSeparator() + "Town" +
                System.lineSeparator() + "Region" +
                System.lineSeparator() + "Postal Code" +
                System.lineSeparator() + "Country";
        Assert.assertEquals(addressBuilder, result);
    }
}