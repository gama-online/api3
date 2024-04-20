package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.entities.PositionSql;
import lt.gama.model.sql.entities.WorkScheduleSql;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2017-03-10.
 */
public class LinkPosition implements LinkBase<PositionSql> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private Auth auth;


    @Override
    public PositionSql resolve(PositionSql document) {
        if (document == null) return null;
        LinkHelper.link(document.getWorkSchedule(), auth.getCompanyId(), WorkScheduleSql.class, dbServiceSQL);
        return document;
    }

    @Override
    public void finish(long documentId) {
    }
}
