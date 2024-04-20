package lt.gama.impexp.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.gama.ConstWorkers;
import lt.gama.helpers.CSVRecordUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.impexp.Csv;
import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.GLAccountSql;
import lt.gama.model.type.enums.DataFormatType;
import lt.gama.model.type.enums.GLAccountType;
import lt.gama.service.StorageService;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MapGLAccount extends MapBase<GLAccountSql> {

	private static final Logger log = LoggerFactory.getLogger(MapGLAccount.class);

	@Serial
	private static final long serialVersionUID = -1L;

	/**
	 * CSV file header
	 */
	protected static final Object[] FILE_HEADER = { "number", "name", "depth", "inner", "type", "parent" };

	private void setGLAccountType(GLAccountSql entity) {
		if (entity.getType() == null && StringHelper.hasValue(entity.getNumber())) {
			switch (entity.getNumber().charAt(0)) {
				case '1' -> entity.setType(GLAccountType.ASSETS);
				case '2' -> entity.setType(GLAccountType.CURRENT_A);
				case '3' -> entity.setType(GLAccountType.EQUITY);
				case '4' -> entity.setType(GLAccountType.LIABILITIES);
				case '5' -> entity.setType(GLAccountType.INCOME);
				case '6' -> entity.setType(GLAccountType.EXPENSES);
			}
		}
	}

	@Override
	public Class<GLAccountSql> getEntityClass() {
		return GLAccountSql.class;
	}

	@Override
	public GLAccountSql importCSV(CSVRecord record) {
		GLAccountSql entity = new GLAccountSql();
		entity.setNumber(CSVRecordUtils.getString(record, "number"));
		entity.setName(CSVRecordUtils.getString(record, "name", "(no-name)"));
		entity.setDepth(CSVRecordUtils.getInt(record, "depth", 1));
		entity.setInner(CSVRecordUtils.getInt(record, "inner", 0) > 0);	// default - 0 (false)
		entity.setType(GLAccountType.from(CSVRecordUtils.getString(record, "type")));
		entity.setParent(CSVRecordUtils.getString(record, "parent"));

		if (StringHelper.isEmpty(entity.getNumber())) {
			log.error(this.getClass().getSimpleName() + ": No account number: " + record);
			return null;
		}

		setGLAccountType(entity);
		return entity;
	}

    @Override
    public GLAccountSql importJSON(ObjectMapper objectMapper, String json) throws IOException {
		GLAccountSql entity = objectMapper.readValue(json, GLAccountSql.class);
		if (entity != null && ((entity.getNumber() == null) || entity.getNumber().isEmpty())) {
			throw new IOException("No account number");
		}
		return entity;
    }

	@Override
	public String transformFile(String fileName, StorageService storageService, DataFormatType dataFormatType) {
		try (
			InputStream is = Channels.newInputStream(storageService.gcsFileReadChannel(fileName))
		) {
			List<Node> nodes = transformInput(is, dataFormatType);
			if (nodes == null)
				return fileName;

			// output
			String outputFileName = ConstWorkers.IMPORT_FOLDER + "/" + java.util.UUID.randomUUID();

			try (
					OutputStream os = Channels.newOutputStream(storageService.gcsFileWriteChannel(outputFileName, null, false, null))
			) {
				transformOutput(os, nodes, dataFormatType);
			}

			// finish
			storageService.deleteFile(fileName);

			return outputFileName;

		} catch (Exception e) {
			log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
			return null;
		}
	}

	protected void transformOutput(OutputStream os, List<Node> nodes, DataFormatType dataFormatType) {
		try (
				OutputStreamWriter printStream = new OutputStreamWriter(os, StandardCharsets.UTF_8);
				CSVPrinter csvFilePrinter = new CSVPrinter(printStream, dataFormatType == DataFormatType.CSV_TAB ? Csv.getCSVFormatTab() : Csv.getCSVFormat())
		) {
			// transform - write
			csvFilePrinter.printRecord(FILE_HEADER);
			for (final Node node : nodes) {
				List<String> dataRecord = new ArrayList<>();

				dataRecord.add(node.getData().getNumber());
				dataRecord.add(node.getData().getName());
				dataRecord.add(String.valueOf(node.getData().getDepth()));
				dataRecord.add(node.getData().isInner() ? "1" : "0");
				dataRecord.add("T");
				dataRecord.add(node.getData().getParent());

				csvFilePrinter.printRecord(dataRecord);
			}
		} catch (IOException e) {
			log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
		}
	}

	protected List<Node> transformInput(InputStream is, DataFormatType dataFormatType) {
		ArrayList<Node> nodes = new ArrayList<>();
		try (
				Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
				CSVParser csvFileParser = new CSVParser(reader, dataFormatType == DataFormatType.CSV_TAB ? Csv.getCSVFormatTab() : Csv.getCSVFormat())
		) {
			Node tree = new Node();

			for (final CSVRecord record : csvFileParser) {

				GLAccountSql entity = importCSV(record);
				if (entity == null)
					continue;

				if (nodes.isEmpty()) {
					nodes.add(tree.addChild(entity));
					continue;
				}

				Node parent = nodes.get(nodes.size() - 1);

                // find parent - start from last
                while (parent != null) {
                    if ((parent.getData() == null) || entity.getNumber().startsWith(parent.getData().getNumber())) {
                        nodes.add(parent.addChild(entity));
                        break;
                    }
                    parent = parent.getParent();
                }
                if (parent == null) {
                    throw new IOException("Bad accounts data structure: can't find parent of " + entity.getNumber());
                }
            }
			return nodes;
		} catch (IOException e) {
			log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
			return null;
		}
	}

	public static class Node {
		private GLAccountSql data;
		private Node parent;
		private List<Node> children;

		public Node() {
		}

		public Node(GLAccountSql data) {
			setData(data);
		}

		public GLAccountSql getData() {
			return data;
		}

		public void setData(GLAccountSql data) {
			this.data = data;
		}

		public Node getParent() {
			return parent;
		}

		private void setParent(Node parent) {
			this.parent = parent;
		}

		public List<Node> getChildren() {
			return children;
		}

		public void setChildren(List<Node> children) {
			this.children = children;
		}

		public Node addChild(GLAccountSql data) throws IOException {
			// check if correct child number
			if ((getParent() != null)
					&& (getParent().getData() != null)
					&& (getParent().getData().getNumber() != null)
					&& !getParent().getData().getNumber().isEmpty()
					&& (data.getNumber() != null)
					&& !data.getNumber().startsWith(getParent().getData().getNumber())) {

				throw new IOException(
						"Bad accounts data structure: wrong parent of "
								+ data.getNumber());
			}

			Node node = new Node(data);
			if (getChildren() == null) setChildren(new ArrayList<>());
			getChildren().add(node);
			node.setParent(this);
			if (this.getData() != null) {
				this.getData().setInner(true);
				node.getData().setParent(this.getData().getNumber());
				node.getData().setDepth(this.getData().getDepth() + 1);
			}
			return node;
		}
	}
}
