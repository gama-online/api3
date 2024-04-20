package lt.gama.api.request;

public class ChangePasswordRequest {

	private String token;

	private String password;

	// generated

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "ChangePasswordRequest{" +
				"token='" + token + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
