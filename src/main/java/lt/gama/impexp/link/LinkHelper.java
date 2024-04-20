package lt.gama.impexp.link;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IExportId;
import lt.gama.model.i.IId;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.DBServiceSQL;

import java.util.List;
import java.util.StringJoiner;

/**
 * Gama
 * Created by valdas on 15-06-10.
 */
public class LinkHelper {

    private LinkHelper() { }  // Prevents instantiation

    public static <E extends IExportId & IId<Long> & IDb> ImportSql link(E doc, long companyId, Class<?>type, DBServiceSQL dbServiceSQL) {
        ImportSql imp = null;
        if (doc == null) return null;

        if (doc.getExportId() != null) {
            Long id = null;
            DBType db = null;
            imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, type, doc.getExportId()));
            if (imp != null) {
                id = imp.getEntityId();
                db = imp.getEntityDb();
            }
            doc.setId(id);
            doc.setDb(db);
        }
        return imp;
    }

    public static void linkCounterparty(CounterpartyDto doc, long companyId, DBServiceSQL dbServiceSQL, EntityManager entityManager) {
        if (doc == null) return;
        link(doc, companyId, CounterpartySql.class, dbServiceSQL);
        if (Validators.isValid(doc)) return;

        if (StringHelper.isEmpty(doc.getComCode()) && StringHelper.isEmpty(doc.getVatCode())) return;

        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT a FROM " + CounterpartySql.class.getName() + " a");
        sj.add("WHERE companyId = :companyId");
        sj.add("AND (archive IS null OR archive = false)");
        if (StringHelper.hasValue(doc.getComCode())) sj.add("AND a.comCode = :comCode");
        else sj.add("AND (a.comCode = '' OR a.comCode IS NULL)");
        if (StringHelper.hasValue(doc.getVatCode())) sj.add("AND a.vatCode = :vatCode");
        else sj.add("AND (a.vatCode = '' OR a.vatCode IS NULL)");

        Query query = entityManager.createQuery(sj.toString());
        query.setParameter("companyId", companyId);
        if (StringHelper.hasValue(doc.getComCode())) query.setParameter("comCode", StringHelper.trim(doc.getComCode()));
        if (StringHelper.hasValue(doc.getVatCode())) query.setParameter("vatCode", StringHelper.trim(doc.getVatCode()));

        @SuppressWarnings("unchecked")
        List<CounterpartySql> counterparties = query.getResultList();
        if (counterparties != null && counterparties.size() > 0) {
            fill(doc, counterparties.get(0));
        }
    }

    private static void fill(CounterpartyDto doc, CounterpartySql c) {
        doc.setId(c.getId());
        doc.setName(c.getName());
        doc.setShortName(c.getShortName());
        doc.setVatCode(c.getVatCode());
        doc.setCreditTerm(c.getCreditTerm());
        doc.setAccounts(c.getAccounts());
        doc.setDb(c.getDb());
    }
}
