package lt.gama.api.request;

import lt.gama.model.type.enums.DBType;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2017-06-07.
 */
public class CalcEmployeeVacationRequest {

    private long id;

    private LocalDate date;

    private DBType db;

    @SuppressWarnings("unused")
    protected CalcEmployeeVacationRequest() {}

    public CalcEmployeeVacationRequest(long id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public CalcEmployeeVacationRequest(long id, LocalDate date, DBType db) {
        this.id = id;
        this.date = date;
        this.db = db;
    }

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public DBType getDb() {
        return db;
    }

    public void setDb(DBType db) {
        this.db = db;
    }

    @Override
    public String toString() {
        return "CalcEmployeeVacationRequest{" +
                "id=" + id +
                ", date=" + date +
                ", db=" + db +
                '}';
    }
}
