package lt.gama.impexp.link;

import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Gama
 * Created by valdas on 15-07-22.
 */
public class LinkDoubleEntry implements LinkBase<DoubleEntrySql> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private DocumentService documentService;

    @Override
    public DoubleEntrySql resolve(DoubleEntrySql document) {
        return document;
    }

    @Override
    public void finish(long documentId) {
        dbServiceSQL.executeInTransaction(em -> documentService.finish(dbServiceSQL.getById(DoubleEntrySql.class, documentId)));
    }
}
