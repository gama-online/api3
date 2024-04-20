package lt.gama.helpers;

@FunctionalInterface
public interface ConsumerWithExeption<P> {
    void accept(P param) throws Exception;
}
