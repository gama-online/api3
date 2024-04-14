package lt.gama.model.i;

/**
 * Gama
 * Created by valdas on 15-07-28.
 */
public interface IMainIndex {

    String getMainIndex();

    default String getSecondIndex() {
        return null;
    }
}
