package lt.gama.model.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.dto.i.IDebtDocumentDto;
import lt.gama.model.type.enums.DebtType;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2016-02-29.
 */
public abstract class BaseDebtDocumentDto extends BaseDocumentDto implements IDebtDocumentDto {

    /**
     * Debt records are created
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean finishedDebt;

    private DebtType debtType;


    /**
     * Check if the document is fully Finished
     * But the document cannot be edited.
     * Must be overridden for a new finishing attribute.
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    public boolean isFullyFinished() {
        return BooleanUtils.isTrue(getFinished()) && BooleanUtils.isTrue(getFinishedDebt());
    }

    /**
     * Set document as fully finished.
     * @return true if document changed, i.e. need to save
     */
    @Override
    public boolean setFullyFinished() {
        boolean changed = BooleanUtils.isNotTrue(getFinished()) || BooleanUtils.isNotTrue(getFinishedDebt());

        setFinished(true);
        finishedDebt = true;

        return changed;
    }

    /**
     * Clear document fully finished flag.
     * @return true if document changed, i.e. need to save
     */
    @Override
    public boolean clearFullyFinished() {
        boolean changed = BooleanUtils.isTrue(getFinished()) || BooleanUtils.isTrue(getFinishedDebt());

        setFinished(null);
        finishedDebt = null;

        return changed;
    }

    @Override
    public boolean isUnfinishedDebt() {
        return BooleanUtils.isNotTrue(finishedDebt);
    }

    @Override
    public LocalDate getDueDate() {
        return getDate();
    }

    @Override
    public Boolean getNoDebt() {
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        finishedDebt = null;
    }

    // generated

    @Override
    public Boolean getFinishedDebt() {
        return finishedDebt;
    }

    @Override
    public void setFinishedDebt(Boolean finishedDebt) {
        this.finishedDebt = finishedDebt;
    }

    public DebtType getDebtType() {
        return debtType;
    }

    public void setDebtType(DebtType debtType) {
        this.debtType = debtType;
    }

    @Override
    public String toString() {
        return "BaseDebtDocumentDto{" +
                "finishedDebt=" + finishedDebt +
                ", debtType=" + debtType +
                "} " + super.toString();
    }
}
