package lt.gama.model.type.part;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.Embeddable;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.cf.CFValue;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-29.
 */
@Embeddable
public class PartSN implements Serializable {

    @Serial
    private static final long serialVersionUID = -1;

    private static final Logger log = LoggerFactory.getLogger(PartSN.class);


    private String sn;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<CFValue> cf;

    @SuppressWarnings("unused")
    protected PartSN() {
    }

    public PartSN(String sn) {
        this.sn = StringHelper.trimNormalize2null(sn);
    }

    public PartSN(String sn, List<CFValue> cf) {
        this(sn);
        this.cf = cf;
    }

    public String getSn() {
        return StringHelper.trimNormalize2null(sn);
    }

    public void setSn(String sn) {
        this.sn = StringHelper.trimNormalize2null(sn);
    }

    public static boolean equals(PartSN sn1, PartSN sn2) {
        if (sn1 == sn2) return true;
        boolean sn1Empty = sn1 == null || (StringHelper.isEmpty(sn1.sn) && isCfEmpty(sn1.cf));
        boolean sn2Empty = sn2 == null || (StringHelper.isEmpty(sn2.sn) && isCfEmpty(sn2.cf));
        return sn1Empty && sn2Empty || !sn1Empty && !sn2Empty && sn1.equals(sn2);
    }

    public static boolean isCfEmpty(List<CFValue> cf) {
        if (CollectionsHelper.isEmpty(cf)) return true;
        return cf.stream().noneMatch(x -> StringHelper.hasValue(x.getValue()));
    }

    public static String makeKey(PartSN partSN) {
//TODO        if (partSN == null) return "";
//
//        // normalize values
//        partSN.setSn(partSN.sn);
//
//        if (CollectionsHelper.hasValue(partSN.getCf())) {
//            partSN.getCf().removeIf(cfValue -> StringHelper.isEmpty(cfValue.getValue()));
//        }
//
//        if (CollectionsHelper.isEmpty(partSN.getCf())) return StringHelper.hasValue(partSN.getSn()) ? partSN.getSn() : "";
//
//        partSN.getCf().sort(Comparator.comparing(CFValue::getKey));
//
//        try {
//            return JsonService.mapper().writeValueAsString(partSN);
//        } catch (JsonProcessingException e) {
//            log.error("PartSN: " + e.getMessage(), e);
//            return null;
//        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartSN partSN = (PartSN) o;
        return Objects.equals(PartSN.makeKey(this), PartSN.makeKey(partSN));
    }

    @Override
    public int hashCode() {
        return Objects.hash(PartSN.makeKey(this));
    }

    @Override
    public String toString() {
        return StringHelper.isEmpty(sn) && CollectionsHelper.isEmpty(cf) ? "-no S/N-"
                : '{' +
                "sn='" + (StringHelper.hasValue(sn) ? sn : "" + '\'') +
                (CollectionsHelper.hasValue(cf) ? ",cf=" + cf : "") +
                '}';
    }

    // generated

    public List<CFValue> getCf() {
        return cf;
    }

    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }
}
