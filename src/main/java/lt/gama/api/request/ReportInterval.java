package lt.gama.api.request;

import java.util.Map;

public enum ReportInterval {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR;

    public static final Map<ReportInterval, String> INTERVAL_PATTERN = Map.of(
            ReportInterval.YEAR, "yyyy",
            ReportInterval.QUARTER, "yyyy, q",
            ReportInterval.MONTH, "yyyy-MM",
            ReportInterval.WEEK, "yyyy, w",
            ReportInterval.DAY, "yyyy-MM-dd");
}
