package lt.gama.tasks;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.StringHelper;

import java.io.Serial;

public class DeleteCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    public DeleteCompanyTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        dbServiceSQL.executeInTransaction(entityManager -> {
            delete("sync", "id");
            delete("companies", "id");
            delete("import", "id_company_id");
            delete("connections");
            delete("counter", "id_company_id");

            delete("assets");

            delete("inventory_history");
            delete("inventory_now");

            delete("debt_coverage");
            delete("debt_history");
            delete("debt_now");

            delete("money_history");

            deleteDocuments("estimate", "estimate_parts");
            deleteDocuments("inventory", "inventory_parts");
            deleteDocuments("inventory_ob", "inventory_ob_parts");
            deleteDocuments("invoice", "invoice_parts");
            deleteDocuments("order", "order_parts");
            deleteDocuments("purchase", "purchase_parts");
            deleteDocuments("trans_prod", "trans_prod_parts");

            deleteDocuments("bank_ob", "bank_ob_balances");
            deleteDocuments("bank_operation");
            deleteDocuments("bank_rate_influences", "bank_rate_influences_bank");

            deleteDocuments("cash_ob", "cash_ob_balances");
            deleteDocuments("cash_operation");
            deleteDocuments("cash_rate_influences", "cash_rate_influences_cash");

            deleteDocuments("employee_ob", "employee_ob_balances");
            deleteDocuments("employee_operation");
            deleteDocuments("employee_rate_influences", "employee_rate_influences_employee");

            deleteDocuments("debt_opening_balances", "debt_ob_counterparties");
            deleteDocuments("debt_corrections");
            deleteDocuments("debt_rate_influences", "debt_rate_influence_counterparties");

            deleteDocuments("gl_opening_balances", "gl_ob_operations");

            deleteDocuments("salary", "employee_charge");

            delete("documents");

            deleteByReference("gl_operations", "parent_id", "double_entries");
            delete("double_entries");

            delete("work_hours");
            delete("employee_absence");
            delete("employee_vacation");
            deleteByReference("employee_roles", "employee_id", "employee");
            delete("roles");

            delete("employee_cards");

            delete("recipe_parts");
            delete("recipe");

            delete("part_parts");
            delete("parts");

            delete("warehouse");
            delete("manufacturer");
            delete("bank_accounts");
            delete("cash");
            delete("counterparties");
            delete("employee");

            delete("positions");
            delete("work_schedules");
            delete("charges");

            delete("gl_accounts");

            delete("resp_centers");
            delete("label");
        });
    }

    private void delete(String tableName) {
        delete(tableName, "company_id");
    }

    private void delete(String tableName, String companyIdFieldName) {
        log.info(className + ": " + tableName);
        int deleted = entityManager
                .createNativeQuery("DELETE FROM \"" + tableName + "\"" +
                        " WHERE " + companyIdFieldName + " = :companyId")
                .setParameter("companyId", getCompanyId())
                .executeUpdate();
        entityManager.flush();
        log.info(className + ": " + tableName + " deleted=" + deleted);
    }

    private void deleteDocuments(String documentTableName) {
        deleteDocuments(documentTableName, null);
    }

    private void deleteDocuments(String documentTableName, String operationTableName) {
        if (StringHelper.hasValue(operationTableName)) {
            log.info(className + ": " + operationTableName);
            int deleted = entityManager
                    .createNativeQuery("DELETE FROM \"" + operationTableName + "\" A" +
                            " USING \"" + documentTableName + "\" M, documents D" +
                            " WHERE A.parent_id = M.id AND M.id = D.id AND D.company_id = :companyId")
                    .setParameter("companyId", getCompanyId())
                    .executeUpdate();
            entityManager.flush();
            log.info(className + ": " + operationTableName + " deleted=" + deleted);
        }
        log.info(className + ": " + documentTableName);
        int deleted = entityManager
                .createNativeQuery("DELETE FROM \"" + documentTableName + "\" A" +
                        " USING documents D" +
                        " WHERE A.id = D.id AND D.company_id = :companyId")
                .setParameter("companyId", getCompanyId())
                .executeUpdate();
        entityManager.flush();
        log.info(className + ": " + documentTableName + " deleted=" + deleted);
    }

    private void deleteByReference(String tableName, String fieldName, String referenceTableName) {
        log.info(className + ": " + tableName);
        int deleted = entityManager
                .createNativeQuery("DELETE FROM \"" + tableName + "\" A" +
                        " USING \"" + referenceTableName + "\" D" +
                        " WHERE A." + fieldName + " = D.id" +
                        " AND D.company_id = :companyId")
                .setParameter("companyId", getCompanyId())
                .executeUpdate();
        entityManager.flush();
        log.info(className + ": " + tableName + " deleted=" + deleted);
    }
}
