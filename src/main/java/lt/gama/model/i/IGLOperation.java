package lt.gama.model.i;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.util.List;

public interface IGLOperation {

    Double getSortOrder();

    GLOperationAccount getDebit();

    GLOperationAccount getCredit();

    GamaMoney getAmount();

    List<DocRC> getDebitRC();

    List<DocRC> getCreditRC();
}
