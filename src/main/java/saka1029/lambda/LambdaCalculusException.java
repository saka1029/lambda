package saka1029.lambda;

public class LambdaCalculusException extends RuntimeException {
    public LambdaCalculusException(String format, Object... args) {
        super(format.formatted(args));
    }

    public LambdaCalculusException(Throwable t) {
        super(t);
    }
}
