package saka1029.lambda;

public class LambdaCalculusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LambdaCalculusException(String format, Object... args) {
        super(format.formatted(args));
    }

    public LambdaCalculusException(Throwable t) {
        super(t);
    }
}
