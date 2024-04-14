package lt.gama.model.type;

import lt.gama.model.type.enums.PackagingType;

import java.io.Serializable;
import java.math.BigDecimal;

public record Packaging (
        PackagingType type,
        String code,
        BigDecimal weight
) implements Serializable {}
