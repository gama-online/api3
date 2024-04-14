package lt.gama.model.i;

import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.LocationUtils;
import lt.gama.model.type.Location;

import java.util.List;

public interface ILocations {

    List<Location> getLocations();

    Location getBusinessAddress();

    Location getRegistrationAddress();

    default Location getPostAddress() { return null; }

    default Location getLocation() {
        return LocationUtils.isValid(getBusinessAddress())
                ? getBusinessAddress()
                : LocationUtils.isValid(getPostAddress())
                ? getPostAddress()
                : !CollectionsHelper.isEmpty(getLocations())
                ? getLocations().get(0)
                : LocationUtils.isValid(getRegistrationAddress())
                ? getRegistrationAddress()
                : null;
    }

    default String getAddress() {
        Location location = getLocation();
        return LocationUtils.isValid(location) ? location.getAddress() : "";
    }
}
