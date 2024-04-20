package lt.gama.api.response;

public interface PageResponseAttr {

    Integer getCursor();

    void setCursor(Integer cursor);

    boolean isMore();

    void setMore(boolean more);

    int getTotal();

    void setTotal(int total);

}
