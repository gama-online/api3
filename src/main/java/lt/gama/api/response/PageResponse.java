package lt.gama.api.response;

import lt.gama.api.request.PageRequest;
import org.springframework.data.domain.Page;

import java.util.List;


public class PageResponse<E, A> implements PageResponseAttr {

    private Integer cursor;

    private boolean more;

    private Integer total;

    private List<E> items;

    private A attachment;

    public void setResponseValues(PageRequest request, int cursor, int responseSize) {
        if (responseSize > request.getPageSize()) {
            setMore(true);
        }
        if (request.isBackward() && cursor == 0) {
            setMore(false);
        }
        if (!request.isBackward()) {
            cursor = cursor + request.getPageSize();
        }
        setCursor(cursor);
    }

    public static <E, A> PageResponse<E, A> of(Page<E> page) {
        PageResponse<E, A> response = new PageResponse<>();
        response.setTotal((int) page.getTotalElements());
        response.setItems(page.getContent());
        response.setCursor((int) page.getPageable().getOffset());
        response.setMore(response.isMore());
        return response;
    }

    // generated

    @Override
    public Integer getCursor() {
        return cursor;
    }

    @Override
    public void setCursor(Integer cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean isMore() {
        return more;
    }

    @Override
    public void setMore(boolean more) {
        this.more = more;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public void setTotal(int total) {
        this.total = total;
    }

    public List<E> getItems() {
        return items;
    }

    public void setItems(List<E> items) {
        this.items = items;
    }

    public A getAttachment() {
        return attachment;
    }

    public void setAttachment(A attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "PageResponse{" +
                "cursor='" + cursor + '\'' +
                ", more=" + more +
                ", total=" + total +
                ", items=" + items +
                ", attachment=" + attachment +
                '}';
    }
}
