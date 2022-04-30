package saka1029.lambda;

public interface Unbind extends AutoCloseable {
    @Override
    void close();
}
