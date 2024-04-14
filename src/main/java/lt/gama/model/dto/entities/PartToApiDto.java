package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BasePartPartApiDto;

import java.io.Serial;
import java.math.BigDecimal;

public class PartToApiDto extends BasePartPartApiDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private BigDecimal costPercent;

    // generated

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public BigDecimal getCostPercent() {
        return costPercent;
    }

    public void setCostPercent(BigDecimal costPercent) {
        this.costPercent = costPercent;
    }

    @Override
    public String toString() {
        return "PartToApiDto{" +
                "recordId=" + recordId +
                ", costPercent=" + costPercent +
                "} " + super.toString();
    }
}
