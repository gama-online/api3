package lt.gama.impexp;

import org.apache.commons.csv.CSVFormat;

public final class Csv {

    /**
     * CSV file format
     */
    static private CSVFormat format = null;

    static public CSVFormat getCSVFormat() { // Comma separated format as defined by RFC 4180
        if (format == null) {
            format = CSVFormat.RFC4180
                    .withHeader()
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .withAllowMissingColumnNames();
        }
        return format;
    }

    static private CSVFormat formatTab = null;

    static public CSVFormat getCSVFormatTab() { // TAB separated format
        if (formatTab == null) {
            formatTab = CSVFormat.TDF
                    .withHeader()
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .withAllowMissingColumnNames();
        }
        return formatTab;
    }
}
