package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BasePartPartApiDto;

import java.io.Serial;

public class PartFromApiDto extends BasePartPartApiDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private String vendorCode;

    // generated

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    @Override
    public String toString() {
        return "PartFromApiDto{" +
                "recordId=" + recordId +
                ", vendorCode='" + vendorCode + '\'' +
                "} " + super.toString();
    }
}
