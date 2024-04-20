package lt.gama.impexp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.gama.model.type.enums.DataFormatType;
import lt.gama.service.StorageService;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;

public abstract class MapBase<E> implements Serializable {

    public abstract Class<E> getEntityClass();

	/**
	 * Map CSV record fields into DB entity attributes
	 * @param record - CSV record
	 * @return filled entity
	 */
    public E importCSV(CSVRecord record) {
        return null;
    }

    /**
     * Map json into DB entity attributes
     * @param json - json string
     * @return filled entity
     */
    public E importJSON(ObjectMapper objectMapper, String json) throws IOException {
        return objectMapper.readValue(json, getEntityClass());
    }

    /**
	 * Export DB entity into JSON
	 * @param entity - DB entity
	 * @return string containing record in JSON format
	 */
    public <T extends E> String exportJSON(ObjectMapper objectMapper, T entity) throws JsonProcessingException {
        return objectMapper.writeValueAsString(entity);
    }

    /**
	 * Transform CSV record before import.
	 * @param record - CSV record
	 * @return the transformed CSV record (can be the same record if nothing was done)
	 */
    public CSVRecord transformCSV(CSVRecord record) {
        return record;
    }

	/**
	 * Transform import file
	 * @param fileName - url of csv data
	 * @return new import file name or the same if nothing was done
	 */
    public String transformFile(String fileName, StorageService storageService, DataFormatType dataFormatType) {
        return fileName;
    }
}
