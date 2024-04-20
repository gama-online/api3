package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.impexp.LinkBase;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.doc.DocPosition;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkEmployeeCardSql implements LinkBase<EmployeeCardSql> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private Auth auth;


    @Override
    public EmployeeCardSql resolve(EmployeeCardSql document) {
        final long companyId = auth.getCompanyId();
        if (document == null || document.getEmployee() == null) return null;
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            ImportSql imp = LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
            if (imp == null) return null;

            EmployeeSql employee = dbServiceSQL.getByIdOrForeignId(EmployeeSql.class, imp.getEntityId(), imp.getEntityDb());
            if (employee != null) {
                if (document.getEmployee().getCf() != null && document.getEmployee().getCf().size() > 0) {
                    employee.setCf(document.getEmployee().getCf());
                }
            }
            document.setEmployee(employee);

            if (document.getPositions() != null && document.getPositions().size() > 0) {
                for (DocPosition position : document.getPositions()) {
                    LinkHelper.link(position, companyId, PositionSql.class, dbServiceSQL);
                    LinkHelper.link(position.getWorkSchedule(), companyId, WorkScheduleSql.class, dbServiceSQL);
                }
            }
            return document;
        });
    }

    @Override
    public void finish(long documentId) {
    }
}
