package lt.gama.test.tools;

import lt.gama.service.AppPropService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AppPropService.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AppPropServiceTest {

    @Autowired
    private AppPropService appPropService;

    @Test
    void testIfTest() {
        assertThat(appPropService.isTest()).isTrue();
    }
}
