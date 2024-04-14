package lt.gama.model.type.auth;

import java.io.Serializable;
import java.time.LocalDate;

public record GpaisSettings(
        String subjectCode,
        String registrationId,
        LocalDate registrationDate
) implements Serializable {}
