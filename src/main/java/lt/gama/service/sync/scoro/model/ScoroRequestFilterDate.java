package lt.gama.service.sync.scoro.model;

import java.time.LocalDateTime;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroRequestFilterDate {

    private LocalDateTime from;

    private LocalDateTime to;


    @SuppressWarnings("unused")
    protected ScoroRequestFilterDate() {}

    public ScoroRequestFilterDate(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    // generated

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "ScoroRequestFilterDate{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
