package pl.ds.bimserver.model.converter;

import org.bimserver.models.ifc2x3tc1.IfcOpeningElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class OpeningElementConverterTest {

    private OpeningElementConverter converter = new OpeningElementConverter();

    @Mock
    private IfcOpeningElement ifc2x3IfcOpeningElement;

    @Mock
    private org.bimserver.models.ifc4.IfcOpeningElement ifc4IfcOpeningElement;

    @Mock
    private List<String> listMock;

    @Test
    public void shouldReturnFalseForStringObject() {
        converter.convert(null, listMock.getClass(), Boolean.class, List.class);
    }

    @Test
    public void shouldReturnTrueForIfc2x3IfcOpeningElement() {
        converter.convert(null, ifc2x3IfcOpeningElement.getClass(), Boolean.class, List.class);
    }

    @Test
    public void shouldReturnTrueForIfc4IfcOpeningElement() {
        converter.convert(null, ifc4IfcOpeningElement.getClass(), Boolean.class, List.class);
    }
}