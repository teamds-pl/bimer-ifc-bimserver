package pl.ds.bimserver.model.converter;

import com.github.dozermapper.core.CustomConverter;
import org.apache.commons.lang3.StringUtils;
import org.bimserver.models.ifc4.IfcPostalAddress;

public class Ifc4AddressConverter implements CustomConverter {

    private static final String NEWLINE = System.lineSeparator();

    @Override
    public Object convert(Object destObj, Object srcObj, Class<?> destClass, Class<?> srcClass) {
        if (!IfcPostalAddress.class.isAssignableFrom(srcClass)) {
            return null;
        }
        return formatAddress((IfcPostalAddress)srcObj);
    }

    private String formatAddress(IfcPostalAddress address) {
        if (address == null) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        boolean firstLine = true;
        firstLine = appendAddressLines(address, str, firstLine);
        firstLine = appendInternalLocation(address, str, firstLine);
        firstLine = appendPostalBox(address, str, firstLine);
        firstLine = appendTown(address, str, firstLine);
        firstLine = appendRegion(address, str, firstLine);
        firstLine = appendPostalCode(address, str, firstLine);
        appendCountry(address, str, firstLine);
        return str.toString();
    }

    private static boolean appendInternalLocation(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String internalLocation = address.getInternalLocation();
        if (StringUtils.isNotBlank(internalLocation)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(internalLocation);
            return false;
        }
        return firstLine;
    }

    private static boolean appendAddressLines(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        boolean skipped = true;
        if (address.getAddressLines() != null) {
            for (String line : address.getAddressLines()) {
                if (StringUtils.isNotBlank(line)) {
                    str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
                    skipped = false;
                    str.append(line);
                }
            }
        }
        return skipped;
    }

    private static void appendCountry(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String country = address.getCountry();
        if (StringUtils.isNotBlank(country)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(country);
        }
    }

    private static boolean appendPostalCode(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String postalCode = address.getPostalCode();
        if (StringUtils.isNotBlank(postalCode)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(postalCode);
            return false;
        }
        return firstLine;
    }

    private static boolean appendRegion(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String region = address.getRegion();
        if (StringUtils.isNotBlank(region)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(region);
            return false;
        }
        return firstLine;
    }

    private static boolean appendTown(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String town = address.getTown();
        if (StringUtils.isNotBlank(town)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(town);
            return false;
        }
        return firstLine;
    }

    private static boolean appendPostalBox(IfcPostalAddress address, StringBuilder str, final boolean firstLine) {
        String postalBox = address.getPostalBox();
        if (StringUtils.isNotBlank(postalBox)) {
            str.append(firstLine ? StringUtils.EMPTY : NEWLINE);
            str.append(NEWLINE).append(postalBox);
            return false;
        }
        return firstLine;
    }
}
