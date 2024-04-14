package lt.gama.helpers;

import lt.gama.model.type.Location;

public final class LocationUtils {

    private LocationUtils() {}

    public static boolean isValid(Location location) {
        if (location == null) return false;
        return StringHelper.hasValue(location.getAddress1()) || StringHelper.hasValue(location.getZip()) ||
                StringHelper.hasValue(location.getCity()) || StringHelper.hasValue(location.getMunicipality()) ||
                StringHelper.hasValue(location.getCountry());
    }

    public static String getAddress(Location location) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringHelper.hasValue(location.getAddress1())) {
            stringBuilder.append(location.getAddress1());
        }
        if (StringHelper.hasValue(location.getAddress2())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(" ");
            stringBuilder.append(location.getAddress2());
        }
        if (StringHelper.hasValue(location.getAddress3())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(" ");
            stringBuilder.append(location.getAddress3());
        }
        if (StringHelper.hasValue(location.getZip())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(", ");
            stringBuilder.append(location.getZip());
        }
        if (StringHelper.hasValue(location.getCity())) {
            if (StringHelper.isEmpty(location.getZip())) {
                if (!stringBuilder.isEmpty()) stringBuilder.append(",");
            }
            if (!stringBuilder.isEmpty()) stringBuilder.append(" ");
            stringBuilder.append(location.getCity());
        }
        if (StringHelper.hasValue(location.getMunicipality())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(", ");
            stringBuilder.append(location.getMunicipality());
        }
        if (StringHelper.hasValue(location.getCountry())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(", ");
            stringBuilder.append(location.getCountry());
        }
        return stringBuilder.toString();
    }

    public static String getStreetAddress(Location location) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringHelper.hasValue(location.getAddress1())) {
            stringBuilder.append(location.getAddress1());
        }
        if (StringHelper.hasValue(location.getAddress2())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(" ");
            stringBuilder.append(location.getAddress2());
        }
        if (StringHelper.hasValue(location.getAddress3())) {
            if (!stringBuilder.isEmpty()) stringBuilder.append(" ");
            stringBuilder.append(location.getAddress3());
        }
        return stringBuilder.toString();
    }
}
