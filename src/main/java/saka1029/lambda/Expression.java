package saka1029.lambda;

public abstract class Expression {
	
	@Override
	public String toString() {
		return LambdaCalculus.string(this);
	}
}
