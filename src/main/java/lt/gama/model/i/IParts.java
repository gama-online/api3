package lt.gama.model.i;

import java.util.List;

public interface IParts<E> {

    List<E> getParts();

    void setParts(List<E> parts);
}
