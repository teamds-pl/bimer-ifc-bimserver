package pl.ds.bimserver.model.converter;

import org.junit.Assert;
import org.junit.Test;

public class ProductTypeConverterTest {

    private ProductTypeConverter converter = new ProductTypeConverter();

    @Test
    public void shouldReturnClassNameWithoutIfcPrefix() {
        Object result = converter.convert("", IfcProductType.class, String.class, IfcProductType.class);
        Assert.assertEquals("ProductType", result);
    }

    @Test
    public void shouldReturnClassNameWithoutImplPostfix() {
        Object result = converter.convert("", ProductTypeImpl.class, String.class, ProductTypeImpl.class);
        Assert.assertEquals("ProductType", result);
    }

    @Test
    public void shouldReturnClassNameWithoutImplPostfixAndImplPostfix() {
        Object result = converter.convert("", IfcProductTypeImpl.class, String.class, IfcProductTypeImpl.class);
        Assert.assertEquals("ProductType", result);
    }

    @Test
    public void shouldReturnClassSimpleNameWithoutModifications() {
        Object result = converter.convert("", ProductType.class, String.class, ProductType.class);
        Assert.assertEquals(ProductType.class.getSimpleName(), result);
    }

    class IfcProductType {}
    class ProductTypeImpl {}
    class IfcProductTypeImpl {}
    class ProductType {}

}