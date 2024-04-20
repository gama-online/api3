package lt.gama.service;

import lt.gama.helpers.StringHelper;

import java.util.Map;

import static java.util.Map.entry;

public final class StorageConst {

    static private final Map<String, String> mimeExt = Map.ofEntries(
            entry("application/json", ".json"),
            entry("text/plain", ".txt"),
            entry("text/xml", ".xml"),
            entry("text/html", ".html"),
            entry("image/jpeg", ".jpeg"),
            entry("image/png", ".png"),
            entry("image/gif", ".gif"),
            entry("application/pdf", ".pdf"),
            entry("application/zip", ".zip"),
            entry("application/msword", ".doc"),
            entry("application/vnd.ms-excel", ".xls"),
            entry("application/vnd.malformations-officedocument.wordprocessingml.document", ".docx"),
            entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
            entry("application/vnd.oasis.opendocument.text", ".odt"),
            entry("application/vnd.oasis.opendocument.spreadsheet", ".ods"));

    private StorageConst() { }

    static public String extensionFromMime(String mimeType) {
        if (StringHelper.isEmpty(mimeType)) return "";
        String ext = mimeExt.get(mimeType.toLowerCase());
        return ext == null ? "" : ext;
    }
}
