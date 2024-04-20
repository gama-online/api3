package lt.gama.helpers;

import lt.gama.model.sql.entities.DebtCoverageSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocDebt;

import java.util.ArrayList;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-01-19.
 */
public final class DebtUtils {

    private DebtUtils() {}

    /**
     * update coverage amount.
     * @param debtCoverage entity
     * @param doc document reference
     * @param amount document amount
     * @param covered covered amount
     */
    public static void debtCoverageUpdateDoc(DebtCoverageSql debtCoverage, Doc doc, GamaMoney amount, GamaMoney covered) {

        debtCoverage.setCovered(GamaMoneyUtils.add(debtCoverage.getCovered(), covered));
        Validators.checkDebtCoverage(debtCoverage);

        if (debtCoverage.getDocs() == null) debtCoverage.setDocs(new ArrayList<>());
        boolean updated = false;
        for (DocDebt docDebt : debtCoverage.getDocs()) {
            if (Objects.equals(docDebt.getDoc().getId(), doc.getId())) {
                updated = true;
                docDebt.setCovered(GamaMoneyUtils.add(docDebt.getCovered(), covered));
                Validators.checkDebtCoverageDocDebt(debtCoverage, docDebt);
                break;
            }
        }
        if (!updated) {
            debtCoverage.getDocs().add(new DocDebt(doc, amount, GamaMoneyUtils.negated(covered)));
        }
    }
}
