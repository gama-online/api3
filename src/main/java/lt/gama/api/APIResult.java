package lt.gama.api;

import com.google.api.gax.rpc.ApiException;
import lt.gama.api.ex.*;
import lt.gama.service.ex.GamaServerErrorException;
import lt.gama.service.ex.rt.*;

import java.net.HttpURLConnection;
import java.time.LocalDateTime;

public class APIResult<T> {

	private T data;

	private Error error;

	private Error warning;

	private final LocalDateTime timestamp = LocalDateTime.now();


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

	public static <T> APIResult<T> Error(int code) {
		APIResult<T> result = new APIResult<>();
		result.setError(new Error(null, code));
		return result;
	}

	public static <T> APIResult<T> Error(String message, int code) {
		APIResult<T> result = new APIResult<>();
		result.setError(new Error(message, code));
		return result;
	}

	// methods

	public static <E> E execute(APIResultFunction<E> function) throws ApiException {
		try {
			return function.run();

		} catch (GamaApiException e) {
			throw e;

		} catch (GamaNotEnoughQuantityException e) {
			throw new GamaApiBadRequestException(e.getMessage(), e.getMessages(), e);

		} catch (GamaNotFoundException e) {
			throw new GamaApiNotFoundException(e.getMessage(), e);

		} catch (GamaUnauthorizedException e) {
			throw new GamaApiUnauthorizedException(e.getMessage(), e);

		} catch (GamaForbiddenException e) {
			throw new GamaApiForbiddenException(e.getMessage(), e);

		} catch (IllegalArgumentException | NullPointerException | GamaException e) {
			throw new GamaApiBadRequestException(e.getMessage(), e);

		} catch (GamaServerErrorException e) {
			throw new GamaApiServerErrorException(e.getMessage(), e);

		} catch (Exception e) {
			throw new GamaApiBadRequestException(e.getMessage(), e);
		}
	}

	public static APIResult<Void> result(APIResultVoidFunction function) throws ApiException {
		return execute(() -> {
			function.run();
			return APIResult.Data();
		});
	}

	public static <T> APIResult<T> result(APIResultFunction<T> function) throws ApiException {
		return APIResult.Data(execute(function));
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
				", timestamp=" + timestamp +
				'}';
	}
}
