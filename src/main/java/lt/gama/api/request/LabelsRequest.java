package lt.gama.api.request;

import lt.gama.model.dto.entities.LabelDto;
import lt.gama.model.type.enums.LabelType;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-03-12.
 */
public class LabelsRequest {

    private List<LabelDto> labels;

    private LabelType type;


    @SuppressWarnings("unused")
    protected LabelsRequest() {}

    public LabelsRequest(List<LabelDto> labels, LabelType type) {
        this.labels = labels;
        this.type = type;
    }

    // generated

    public List<LabelDto> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelDto> labels) {
        this.labels = labels;
    }

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LabelsRequest{" +
                "labels=" + labels +
                ", type=" + type +
                '}';
    }
}
