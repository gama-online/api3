//package lt.gama.api.ex;
//
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
////    @ExceptionHandler(GamaApiException.class)
////    RestErrorResponse handleGamaApiException(GamaApiException ex) {
////        return new RestErrorResponse(
////                ex.getStatusCode(),
////                ex.getMessage(),
////                LocalDateTime.now());
////    }
//
//    @ExceptionHandler(GamaApiException.class)
//    public void handleGamaApiException(HttpServletResponse response, GamaApiException ex) throws IOException {
//        response.setStatus(ex.getStatusCode());
//        response.getWriter().write(new RestErrorResponse(ex.getStatusCode(), ex.getMessage(), LocalDateTime.now()));
//    }
//
//    // Handle any other exception too.
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    RestErrorResponse handleException(Exception ex) {
//        return new RestErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                ex.getMessage(),
//                LocalDateTime.now());
//    }
//
//    record RestErrorResponse(int status, String message, LocalDateTime timestamp) {}
//}
