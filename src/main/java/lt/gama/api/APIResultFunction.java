package lt.gama.api;

@FunctionalInterface
public interface APIResultFunction<E> {

    E run() throws Exception;

}
