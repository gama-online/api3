package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DepreciationService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2015-11-06.
 */
public class LinkAsset implements LinkBase<AssetSql> {

    @Autowired
    DBServiceSQL dbServiceSQL;

    @Autowired
    DepreciationService depreciationService;

    @Autowired
    private Auth auth;


    @Override
    public AssetSql resolve(AssetSql document) {
        if (document == null) return null;
        if (Validators.isValid(document.getResponsible())) {
            LinkHelper.link(document.getResponsible(), auth.getCompanyId(), EmployeeSql.class, dbServiceSQL);
        }
        if (document.getHistory() != null) {
            for (AssetHistory history : document.getHistory()) {
                LinkHelper.link(history.getResponsible(), auth.getCompanyId(), EmployeeSql.class, dbServiceSQL);
            }
        }
        depreciationService.reset(document);
        return document;
    }

    @Override
    public void finish(long documentId) {
    }
}