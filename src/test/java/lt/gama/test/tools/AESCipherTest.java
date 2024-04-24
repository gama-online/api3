package lt.gama.test.tools;

import lt.gama.auth.service.AESCipherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AESCipherService.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AESCipherTest {

	@Autowired
	private AESCipherService aesCipherService;

	@Test
	public void testAESCipher() throws GeneralSecurityException {
		String raw = "test string";
		String encrypt = aesCipherService.encrypt(raw);
		assertThat(encrypt).isNotNull();
		assertThat(raw).isNotEqualTo(encrypt);
		String decrypt = aesCipherService.decrypt(encrypt);
		assertThat(raw).isEqualTo(decrypt);
	}
}
