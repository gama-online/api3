package lt.gama.test.tools;

import lt.gama.helpers.DateUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.type.auth.CompanySettings;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * gama-online
 * Created by valdas on 2018-06-05.
 */
public class ValidatorsTest {

    @Test
    public void testCheckDocumentDate() {
        // set system time and time zone
        DateUtils.mockClock = Clock.fixed(
                LocalDateTime.of(2018, 4, 15, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());

        CompanySettings settings = new CompanySettings();
        settings.setAccYear(2017);
        settings.setAccMonth(1);

        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2017, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2018, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2019, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2019, 12, 31)));

        assertThrows(IllegalArgumentException.class, () -> Validators.checkDocumentDate(settings, LocalDate.of(2016, 12, 31)));
        assertThrows(IllegalArgumentException.class, () -> Validators.checkDocumentDate(settings, LocalDate.of(2023, 1, 1)));

        settings.setAccYear(2010);

        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2010, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2012, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2019, 12, 31)));

        assertThrows(IllegalArgumentException.class, () -> Validators.checkDocumentDate(settings, LocalDate.of(2009, 12, 31)));
        assertThrows(IllegalArgumentException.class, () -> Validators.checkDocumentDate(settings, LocalDate.of(2023, 1, 1)));

        // change current date
        DateUtils.mockClock = Clock.fixed(
                LocalDateTime.of(2019, 4, 15, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());

        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2020, 1, 1)));
        assertDoesNotThrow(() -> Validators.checkDocumentDate(settings, LocalDate.of(2020, 12, 31)));

        assertThrows(IllegalArgumentException.class, () -> Validators.checkDocumentDate(settings, LocalDate.of(2024, 1, 1)));
    }
}
