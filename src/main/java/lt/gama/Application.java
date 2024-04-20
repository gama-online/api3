package lt.gama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		String port = System.getenv("PORT");
		if (port == null) port = "8083";
		SpringApplication.run(Application.class, "--server.port=" + port);
	}
}
