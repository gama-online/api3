package lt.gama.impexp.link;

import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.documents.GLOpeningBalanceSql;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2018-09-21.
 */
public class LinkGLOpeningBalance implements LinkBase<GLOpeningBalanceSql> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private DocumentService documentService;

    @Override
    public GLOpeningBalanceSql resolve(GLOpeningBalanceSql document) {
        return document;
    }

    @Override
    public void finish(long documentId) {
        dbServiceSQL.executeInTransaction(em -> documentService.finish(dbServiceSQL.getById(GLOpeningBalanceSql.class, documentId)));
    }
}
