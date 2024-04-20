package lt.gama.api.service;

import jakarta.servlet.http.HttpServletResponse;
import lt.gama.api.request.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.IApi.APP_API_3_PATH;

@RequestMapping({ APP_API_3_PATH + "/abc", "/abc" })
public interface TestAuthApi {

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response);

}
