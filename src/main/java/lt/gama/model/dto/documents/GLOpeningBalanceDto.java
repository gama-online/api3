package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseNumberDocumentDto;
import lt.gama.model.dto.documents.items.GLOpeningBalanceOperationDto;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class GLOpeningBalanceDto extends BaseNumberDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private Boolean iBalance;

    private List<GLOpeningBalanceOperationDto> balances;

    /**
     * finished or unfinished G.L. operations - can be finished separately from all others
     */
    private Boolean finishedGL;

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public Boolean getFinished() {
        return getFinishedGL();
    }

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public Boolean getFullyFinished() {
        return getFinishedGL();
    }


    @Override
    public void reset() {
        super.reset();
        finishedGL = false;
    }

    public Boolean getIBalance() {
        return iBalance;
    }

    public void setIBalance(Boolean iBalance) {
        this.iBalance = iBalance;
    }

    // generated
    // except for iBalance

    public List<GLOpeningBalanceOperationDto> getBalances() {
        return balances;
    }

    public void setBalances(List<GLOpeningBalanceOperationDto> balances) {
        this.balances = balances;
    }

    public Boolean getFinishedGL() {
        return finishedGL;
    }

    public void setFinishedGL(Boolean finishedGL) {
        this.finishedGL = finishedGL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GLOpeningBalanceDto that = (GLOpeningBalanceDto) o;
        return Objects.equals(iBalance, that.iBalance) && Objects.equals(balances, that.balances) && Objects.equals(finishedGL, that.finishedGL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), iBalance, balances, finishedGL);
    }

    @Override
    public String toString() {
        return "GLOpeningBalanceDto{" +
                "iBalance=" + iBalance +
                ", balances=" + balances +
                ", finishedGL=" + finishedGL +
                "} " + super.toString();
    }
}
