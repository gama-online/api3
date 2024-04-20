package lt.gama.tasks.maintenance;

import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.io.Serializable;

public class PartGLAccount implements Serializable {

    private GLOperationAccount accountAsset;

    private GLDC glIncome;

    private GLDC glExpense;

    // generated

    public GLOperationAccount getAccountAsset() {
        return accountAsset;
    }

    public void setAccountAsset(GLOperationAccount accountAsset) {
        this.accountAsset = accountAsset;
    }

    public GLDC getGlIncome() {
        return glIncome;
    }

    public void setGlIncome(GLDC glIncome) {
        this.glIncome = glIncome;
    }

    public GLDC getGlExpense() {
        return glExpense;
    }

    public void setGlExpense(GLDC glExpense) {
        this.glExpense = glExpense;
    }

    @Override
    public String toString() {
        return "PartGLAccount{" +
                "accountAsset=" + accountAsset +
                ", glIncome=" + glIncome +
                ", glExpense=" + glExpense +
                '}';
    }
}
