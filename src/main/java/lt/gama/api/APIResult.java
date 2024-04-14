package lt.gama.api;

import java.net.HttpURLConnection;

public class APIResult<T> {

	private T data;

	private Error error;

	private Error warning;


	public static <T> APIResult<T> Data() {
		return Data(null);
	}

	public static <T> APIResult<T> Data(T data) {
		APIResult<T> result = new APIResult<>();
		result.setData(data);
		return result;
	}

    public static <T> APIResult<T> Data(T data, String message, int code) {
		APIResult<T> result = new APIResult<>();
		result.setData(data);
		result.setWarning(new Error(message, code));
		return result;
    }

    public static <T> APIResult<T> Error() {
        return Error(null, HttpURLConnection.HTTP_BAD_REQUEST);
    }

	public static <T> APIResult<T> Error(String message) {
		return Error(message, HttpURLConnection.HTTP_BAD_REQUEST);
	}

	public static <T> APIResult<T> Error(String message, int code) {
		APIResult<T> result = new APIResult<>();
		result.setError(new Error(message, code));
		return result;
	}

	// generated

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Error getWarning() {
		return warning;
	}

	public void setWarning(Error warning) {
		this.warning = warning;
	}

	@Override
	public String toString() {
		return "APIResult{" +
				"data=" + data +
				", error=" + error +
				", warning=" + warning +
				'}';
	}
}
