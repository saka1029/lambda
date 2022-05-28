package saka1029.lambda;

import static org.junit.Assert.*;
import static saka1029.lambda.LambdaCalculus.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestExpand {

	static void define(Map<FreeVariable, Expression> context, String freeVariableName, String expression) {
		context.put(FreeVariable.of(freeVariableName), parse(expression));
	}
	
	static void testExpand(Map<FreeVariable, Expression> context, String expected, String actual) {
		Expression e = parse(expected);
		Expression a = expand(parse(actual), context);
		assertEquals(string(e), string(a));
	}

	@Test
	public void test() {
		Map<FreeVariable, Expression> context = new HashMap<>();
		define(context, "0", "λf x.x");
		define(context, "1", "λf x.f x");
		define(context, "2", "λf x.f (f x)");
		define(context, "3", "λf x.f (f (f x))");
		define(context, "SUCC", "λn f x.f (n f x)");
		define(context, "PLUS", "λm n f x.m f (n f x)");
		testExpand(context, "(λn f x.f (n f x)) λf x.x", "SUCC 0");
		testExpand(context, "(λn f x.f (n f x)) ((λn f x.f (n f x)) λf x.x)", "SUCC (SUCC 0)");
	}

}
