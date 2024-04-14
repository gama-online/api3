package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DocHeader implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private String docId;
    private int docCorrNo;
    private LocalDate completionDate;

    public void incDocCorrNo() {
        ++docCorrNo;
    }

    // generated

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public int getDocCorrNo() {
        return docCorrNo;
    }

    public void setDocCorrNo(int docCorrNo) {
        this.docCorrNo = docCorrNo;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocHeader docHeader = (DocHeader) o;
        return docCorrNo == docHeader.docCorrNo && Objects.equals(docId, docHeader.docId) && Objects.equals(completionDate, docHeader.completionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, docCorrNo, completionDate);
    }

    @Override
    public String toString() {
        return "DocHeader{" +
                "docId='" + docId + '\'' +
                ", docCorrNo=" + docCorrNo +
                ", completionDate=" + completionDate +
                '}';
    }
}
