package lt.gama.service;

import lt.gama.api.APIResult;
import lt.gama.api.APIResultFunction;
import lt.gama.api.APIResultVoidFunction;
import lt.gama.api.ex.*;
import lt.gama.service.ex.GamaServerErrorException;
import lt.gama.service.ex.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class APIResultService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public <E> E execute(APIResultFunction<E> function) throws GamaApiException {
        try {
            return function.run();

        } catch (GamaApiException e) {
            throw e;

        } catch (GamaNotEnoughQuantityException e) {
            throw new GamaApiBadRequestException(e.getMessage(), e.getMessages(), e);

        } catch (GamaNotFoundException e) {
            log.info(e.toString(), e);
            throw new GamaApiNotFoundException(e.getMessage(), e);

        } catch (GamaUnauthorizedException e) {
            log.info(e.toString(), e);
            throw new GamaApiUnauthorizedException(e.getMessage(), e);

        } catch (GamaForbiddenException e) {
            log.info(e.toString(), e);
            throw new GamaApiForbiddenException(e.getMessage(), e);

        } catch (IllegalArgumentException | NullPointerException | GamaException e) {
            log.info(e.toString(), e);
            throw new GamaApiBadRequestException(e.getMessage(), e);

        } catch (GamaServerErrorException e) {
            log.error(e.toString(), e);
            throw new GamaApiServerErrorException(e.getMessage(), e);

        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new GamaApiBadRequestException(e.getMessage(), e);
        }
    }

    public APIResult<Void> result(APIResultVoidFunction function) throws GamaApiException {
        return execute(() -> {
            function.run();
            return APIResult.Data();
        });
    }

    public <T> APIResult<T> result(APIResultFunction<T> function) throws GamaApiException {
        return APIResult.Data(execute(function));
    }
}
