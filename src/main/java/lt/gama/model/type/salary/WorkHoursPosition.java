package lt.gama.model.type.salary;

import lt.gama.model.type.doc.DocPosition;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * gama-online
 * Created by valdas on 2016-12-16.
 */
public class WorkHoursPosition implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Position with independent work schedule
     */
    private DocPosition position;

    /**
     * Other positions with the same work schedule (aggregate = "true")
     */
    private List<DocPosition> subPositions;

    /**
     * List of all days in accounting period (month) with work time codes for each day
     */
    private List<WorkHoursDay> period;

    private WorkData workData;


    public WorkHoursPosition() {
    }

    public WorkHoursPosition(WorkHoursPosition workHoursPosition) {
        if (workHoursPosition == null) return;
        this.position = new DocPosition(workHoursPosition.getPosition());
        if (workHoursPosition.getSubPositions() != null) {
            this.subPositions = workHoursPosition.getSubPositions().stream().map(DocPosition::new).collect(Collectors.toList());
        }
        this.workData = new WorkData(workHoursPosition.workData);
    }

    public WorkHoursPosition(DocPosition position, List<DocPosition> subPositions, List<WorkHoursDay> period, WorkData workData) {
        this.position = position;
        this.subPositions = subPositions;
        this.period = period;
        this.workData = workData;
    }

    // generated

    public DocPosition getPosition() {
        return position;
    }

    public void setPosition(DocPosition position) {
        this.position = position;
    }

    public List<DocPosition> getSubPositions() {
        return subPositions;
    }

    public void setSubPositions(List<DocPosition> subPositions) {
        this.subPositions = subPositions;
    }

    public List<WorkHoursDay> getPeriod() {
        return period;
    }

    public void setPeriod(List<WorkHoursDay> period) {
        this.period = period;
    }

    public WorkData getWorkData() {
        return workData;
    }

    public void setWorkData(WorkData workData) {
        this.workData = workData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkHoursPosition that = (WorkHoursPosition) o;
        return Objects.equals(position, that.position) && Objects.equals(subPositions, that.subPositions) && Objects.equals(period, that.period) && Objects.equals(workData, that.workData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, subPositions, period, workData);
    }

    @Override
    public String toString() {
        return "WorkHoursPosition{" +
                "position=" + position +
                ", subPositions=" + subPositions +
                ", period=" + period +
                ", workData=" + workData +
                '}';
    }
}
