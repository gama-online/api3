package lt.gama.api.response;

import lt.gama.model.type.Packaging;

import java.math.BigDecimal;
import java.util.List;

public record PackInfo(
        BigDecimal quantity,
        List<Packaging> packaging) {}
