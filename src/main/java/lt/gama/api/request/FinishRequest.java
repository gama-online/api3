package lt.gama.api.request;

public
class FinishRequest {
    public long id;
    public Boolean finishGL;


    public FinishRequest() {
    }

    public FinishRequest(long id, Boolean finishGL) {
        this.id = id;
        this.finishGL = finishGL;
    }

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Boolean getFinishGL() {
        return finishGL;
    }

    public void setFinishGL(Boolean finishGL) {
        this.finishGL = finishGL;
    }

    @Override
    public String toString() {
        return "FinishRequest{" +
                "id=" + id +
                ", finishGL=" + finishGL +
                '}';
    }
}
