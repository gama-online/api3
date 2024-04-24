package lt.gama.test.tools;

import lt.gama.helpers.LocationUtils;
import lt.gama.model.type.Location;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2018-08-31.
 */
class LocationTest {

    @Test
    void testLocationAddressFull() {

        Location location = new Location();
        location.setAddress1("Address 1");
        location.setAddress2("Address 2");
        location.setAddress3("Address 3");
        location.setZip("Zip");
        location.setCity("City");
        location.setMunicipality("Municipality");
        location.setCountry("Country");

        assertThat(LocationUtils.isValid(location)).isTrue();
        assertThat(LocationUtils.getAddress(location)).isEqualTo("Address 1 Address 2 Address 3, Zip City, Municipality, Country");
    }

    @Test
    void testLocationAddressPartial() {
        {
            Location location = new Location();
            location.setAddress1("Address 1");
            location.setAddress2("Address 2");
            location.setZip("Zip");
            location.setCity("City");
            location.setMunicipality("Municipality");
            location.setCountry("Country");

            assertThat(LocationUtils.isValid(location)).isTrue();
            assertThat(LocationUtils.getAddress(location)).isEqualTo("Address 1 Address 2, Zip City, Municipality, Country");
        }
        {
            Location location = new Location();
            location.setAddress1("Address 1");
            location.setCity("City");
            location.setMunicipality("Municipality");
            location.setCountry("Country");

            assertThat(LocationUtils.isValid(location)).isTrue();
            assertThat(LocationUtils.getAddress(location)).isEqualTo("Address 1, City, Municipality, Country");
        }
        {
            Location location = new Location();
            location.setAddress1("Address 1");
            location.setCity("City");
            location.setCountry("Country");

            assertThat(LocationUtils.isValid(location)).isTrue();
            assertThat(LocationUtils.getAddress(location)).isEqualTo("Address 1, City, Country");
        }
        {
            Location location = new Location();
            location.setAddress1("Address 1");
            location.setAddress3("Address 3");
            location.setZip("Zip");
            location.setCity("City");

            assertThat(LocationUtils.isValid(location)).isTrue();
            assertThat(LocationUtils.getAddress(location)).isEqualTo("Address 1 Address 3, Zip City");
        }
    }

    @Test
    void testLocationAddressEmpty() {

        Location location = new Location();
        location.setAddress1("");
        location.setAddress2("");
        location.setAddress3("");
        location.setZip("");
        location.setCity("");
        location.setMunicipality("");
        location.setCountry("");

        assertThat(LocationUtils.isValid(location)).isEqualTo(false);
        assertThat(LocationUtils.getAddress(location)).isEqualTo("");

        location = new Location();
        assertThat(LocationUtils.isValid(location)).isEqualTo(false);
        assertThat(LocationUtils.getAddress(location)).isEqualTo("");
    }

    @Test
    void testLocationValidity() {

        assertThat(LocationUtils.isValid(null)).isEqualTo(false);

        Location location = new Location();
        location.setAddress1("");
        location.setAddress2("");
        location.setAddress3("");
        location.setZip("");
        location.setCity("");
        location.setMunicipality("");
        location.setCountry("");

        assertThat(LocationUtils.isValid(location)).isEqualTo(false);

        location.setZip("1");
        assertThat(LocationUtils.isValid(location)).isTrue();

        location.setZip(null);
        location.setAddress2("1");
        location.setAddress3("2");
        assertThat(LocationUtils.isValid(location)).isEqualTo(false);

        location.setAddress1("1");
        assertThat(LocationUtils.isValid(location)).isTrue();

        location.setAddress1(null);
        assertThat(LocationUtils.isValid(location)).isEqualTo(false);
        location.setMunicipality("1");
        assertThat(LocationUtils.isValid(location)).isTrue();

        location.setMunicipality(null);
        assertThat(LocationUtils.isValid(location)).isEqualTo(false);
        location.setCountry("1");
        assertThat(LocationUtils.isValid(location)).isTrue();
    }
}
