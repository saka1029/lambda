package saka1029.lambda;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static saka1029.lambda.LambdaCalculus.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestLambdaCalculusReduce {
	
	static void define(Map<FreeVariable, Expression> context, String freeVariableName, String expression) {
		context.put(FreeVariable.of(freeVariableName), parse(expression));
	}

	static void equals(Map<FreeVariable, Expression> context, String expected, String actual) {
		assertEquals(reduce(parse(expected), context).toString(),
                     reduce(parse(actual), context).toString());
	}

	/**
	 * ラムダ計算#自然数と算術 - Wikipedia
	 * https://ja.wikipedia.org/wiki/%E3%83%A9%E3%83%A0%E3%83%80%E8%A8%88%E7%AE%97#%E8%87%AA%E7%84%B6%E6%95%B0%E3%81%A8%E7%AE%97%E8%A1%93
	 */
	@Test
	public void testChurchNumerals() {
		Map<FreeVariable, Expression> context = new HashMap<>();
		define(context, "0", "λf x.x");
		define(context, "1", "λf x.f x");
		define(context, "2", "λf x.f (f x)");
		define(context, "3", "λf x.f (f (f x))");
		define(context, "SUCC", "λn f x.f (n f x)");
		/*
		 * SUCC 0 {}
		 * = (λn f x.f (n f x))(λf x.x)
		 * = λf x.f (n f x) {n=λf x.x}
		 * = λf x.f ((λf x.x) f x) {n=λf x.x}
		 * = λf x.f x
		 */
//		equals(context, "1", "SUCC 0");
		equals(context, "2", "SUCC 1");
		equals(context, "2", "SUCC (SUCC 0)");
		define(context, "PLUS", "λm n f x.m f (n f x)");
		equals(context, "1", "PLUS 0 1");
		equals(context, "2", "PLUS 1 1");
		equals(context, "3", "PLUS 1 2");
		equals(context, "3", "PLUS 1 (SUCC 1)");
		define(context, "PLUS2", "λm n.m SUCC n");
		equals(context, "1", "PLUS2 0 1");
		equals(context, "2", "PLUS2 1 1");
		equals(context, "3", "PLUS2 1 2");
		define(context, "MULT", "λm n.m (PLUS n) 0");
//		equals(context, "0", "MULT 0 1");
		equals(context, "1", "MULT 1 1");
		equals(context, "2", "MULT 1 2");
		define(context, "PRED", "λn f x.n (λg h.h (g f)) (λu.x) (λu.u)");
//		equals(context, "1", "PRED 2");
//		equals(context, "2", "PRED 3");
//		equals(context, "2", "PRED (PRED 3)");
		define(context, "PRED2", " λn.n (λg k.(g 1) (λu.PLUS (g k) 1) k) (λv.0) 0");
//		equals(context, "1", "PRED2 2");
//		equals(context, "2", "PRED2 3");
//		equals(context, "2", "PRED2 (PRED2 3)");
	}

}
