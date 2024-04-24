package lt.gama.test.tools;

import lt.gama.model.sql.documents.DebtOpeningBalanceSql;
import lt.gama.model.type.doc.Doc;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DocUuidTest {

    private final UUID uuid = UUID.randomUUID();

    @Test
    void testDocSql() {
        DebtOpeningBalanceSql doc = new DebtOpeningBalanceSql();
        doc.setUuid(uuid);

        assertThat(Doc.of(doc).getUuid()).isEqualTo(uuid);
    }
}
