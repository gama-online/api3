package lt.gama.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.helpers.LocationUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

	private String name;

	private String address1;

	private String address2;

	private String address3;

	private String zip;

	private String city;

	private String municipality;

    /**
     * ISO Alpha 2 country code
     */
	private String country;

	public Location() {
	}

	public Location(String name, String address1, String address2, String address3, String zip, String city, String municipality, String country) {
		this.name = name;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.zip = zip;
		this.city = city;
		this.municipality = municipality;
		this.country = country;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getAddress() {
        return LocationUtils.getAddress(this);
    }

	// fluent setters

	public Location setName(String name) {
		this.name = name;
		return this;
	}

	public Location setAddress1(String address1) {
		this.address1 = address1;
		return this;
	}

	public Location setAddress2(String address2) {
		this.address2 = address2;
		return this;
	}

	public Location setAddress3(String address3) {
		this.address3 = address3;
		return this;
	}

	public Location setZip(String zip) {
		this.zip = zip;
		return this;
	}

	public Location setCity(String city) {
		this.city = city;
		return this;
	}

	public Location setMunicipality(String municipality) {
		this.municipality = municipality;
		return this;
	}

	public Location setCountry(String country) {
		this.country = country;
		return this;
	}


	// generated
	// except setters

	public String getName() {
		return name;
	}

	public String getAddress1() {
		return address1;
	}

	public String getAddress2() {
		return address2;
	}

	public String getAddress3() {
		return address3;
	}

	public String getZip() {
		return zip;
	}

	public String getCity() {
		return city;
	}

	public String getMunicipality() {
		return municipality;
	}

	public String getCountry() {
		return country;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location location = (Location) o;
		return Objects.equals(name, location.name) && Objects.equals(address1, location.address1) && Objects.equals(address2, location.address2) && Objects.equals(address3, location.address3) && Objects.equals(zip, location.zip) && Objects.equals(city, location.city) && Objects.equals(municipality, location.municipality) && Objects.equals(country, location.country);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, address1, address2, address3, zip, city, municipality, country);
	}

	@Override
	public String toString() {
		return "Location{" +
				"name='" + name + '\'' +
				", address1='" + address1 + '\'' +
				", address2='" + address2 + '\'' +
				", address3='" + address3 + '\'' +
				", zip='" + zip + '\'' +
				", city='" + city + '\'' +
				", municipality='" + municipality + '\'' +
				", country='" + country + '\'' +
				'}';
	}
}
