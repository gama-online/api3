package lt.gama.test.tools;

import lt.gama.auth.service.AESCipherService;
import lt.gama.model.type.l10n.LangEmployee;
import lt.gama.service.TranslationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2018-09-17.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TranslationService.class)
@TestPropertySource(locations = "classpath:application.properties")
public class TranslationServiceTest {

    @Autowired
    private TranslationService translationService;

    @Test
    public void testTranslate() {

        Map<String, LangEmployee> translations = new HashMap<>();

        LangEmployee langEmployee = new LangEmployee();
        langEmployee.setLanguage("en");
        langEmployee.setOffice("Boss");
        translations.put(langEmployee.getLanguage(), langEmployee);

        langEmployee = new LangEmployee();
        langEmployee.setLanguage("lt");
        langEmployee.setOffice("Bosas");
        translations.put(langEmployee.getLanguage(), langEmployee);

        assertThat(translationService.translate("original", translations, "fr", "office")).isEqualTo("original");
        assertThat(translationService.translate("original", translations, "en", "office")).isEqualTo("Boss");
        assertThat(translationService.translate("original", translations, "lt", "office")).isEqualTo("Bosas");
    }
}

