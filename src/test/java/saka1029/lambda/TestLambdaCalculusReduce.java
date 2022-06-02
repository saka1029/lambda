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
//		自然数をラムダ式で表現する方法はいくつか異なる手法が知られているが、その中でもっとも一般的なのはチャーチ数（英語版）
//		（英: Church numerals）と呼ばれるもので、以下のように定義されている。
		define(context, "0", "λf x.x");
		define(context, "1", "λf x.f x");
		define(context, "2", "λf x.f (f x)");
		define(context, "3", "λf x.f (f (f x))");
//		以下同様である。直感的には、数 n はラムダ式では f という関数をもらってそれを n 回適用したものを返す関数である。
//		つまり、チャーチ数は1引数関数を受け取り、1引数関数を返す高階関数である。
//		（チャーチの提唱した元々のラムダ計算は、ラムダ式の引数が少なくとも一回は関数の本体に出現していなくてはならないことになっていた。
//		そのため、その体系では上に挙げた 0 の定義は不可能である。）
//		上のチャーチ数の定義のもとで、後続（後者）を計算する関数、すなわち n を受け取って n + 1 を返す関数を定義することができる。
//		それは以下のようになる。
		define(context, "SUCC", "λn f x.f (n f x)");
		equals(context, "1", "SUCC 0");
		equals(context, "2", "SUCC 1");
		equals(context, "2", "SUCC (SUCC 0)");
		equals(context, "3", "SUCC (SUCC 1)");
//		また、加算は以下のように定義できる。
		define(context, "PLUS", "λm n f x.m f (n f x)");
		equals(context, "1", "PLUS 0 1");
		equals(context, "2", "PLUS 1 1");
		equals(context, "3", "PLUS 1 2");
		equals(context, "3", "PLUS 1 (SUCC 1)");
//		または単にSUCCを用いて、以下のように定義してもよい。
		define(context, "PLUS2", "λm n.m SUCC n");
		equals(context, "1", "PLUS2 0 1");
		equals(context, "2", "PLUS2 1 1");
		equals(context, "3", "PLUS2 1 2");
//		PLUS は2つの自然数をとり1つの自然数を返す関数である。
//		この理解のためには例えば、 PLUS 2 3 == 5 であることを確認してみるとよいだろう。
//		また、乗算は以下のように定義される。
		define(context, "MULT", "λm n.m (PLUS n) 0");
		equals(context, "0", "MULT 0 1");
		equals(context, "1", "MULT 1 1");
		equals(context, "2", "MULT 1 2");
//		この定義は、 m と n の乗算は、 0 に n を m回加えることと等しい、ということを利用して作られている。
//		もう少し短く、以下のように定義することもできる。
		define(context, "MULT2", "λm n f. m (n f)");
		equals(context, "0", "MULT2 0 1");
		equals(context, "1", "MULT2 1 1");
		equals(context, "2", "MULT2 1 2");
//		正の整数 n の先行（前者）を計算する関数 PRED n = n − 1 は簡単ではなく、
		define(context, "PRED", "λn f x.n (λg h.h (g f)) (λu.x) (λu.u)");
		equals(context, "1", "PRED 2");
		equals(context, "2", "PRED 3");
		equals(context, "1", "PRED (PRED 3)");
//		もしくは
		define(context, "PRED2", " λn.n (λg k.(g 1) (λu.PLUS (g k) 1) k) (λv.0) 0");
//		と定義される。
//		上の部分式 (g 1) (λu. PLUS (g k) 1) k は、 g(1) がゼロとなるとき k に評価され、
//		そうでないときは g(k) + 1 に評価されることに注意せよ[1]。
		equals(context, "1", "PRED2 2");
		equals(context, "2", "PRED2 3");
		equals(context, "1", "PRED2 (PRED2 3)");
	}

	/**
	 * ラムダ計算#論理記号と述語 - Wikipedia
	 * https://ja.wikipedia.org/wiki/%E3%83%A9%E3%83%A0%E3%83%80%E8%A8%88%E7%AE%97#%E8%AB%96%E7%90%86%E8%A8%98%E5%8F%B7%E3%81%A8%E8%BF%B0%E8%AA%9E
	 */
	@Test
	public void testChurchBooleans() {
		Map<FreeVariable, Expression> context = new HashMap<>();
//		TRUE や FALSE といった真理値は慣習的に以下のように定義されることが多い。
//		これらはチャーチ真理値（英語版）（英: Church booleans）とよばれている。
//		これらの真理値に対して論理記号を定義することができる。たとえば、以下のようなものがある。
		define(context, "TRUE", "λx y.x"); // （この FALSE は前述のチャーチ数のゼロと同じ定義であることに注意せよ）
		define(context, "FALSE", "λx y.y");
		define(context, "AND", "λp q.p q FALSE");
		define(context, "OR", "λp q.p TRUE q");
		define(context, "NOT", "λp.p FALSE TRUE");
		define(context, "IFTHENELSE", "λp x y.p x y");
		equals(context, "TRUE", "AND TRUE TRUE");
		equals(context, "FALSE", "AND TRUE FALSE");
		equals(context, "FALSE", "AND FALSE TRUE");
		equals(context, "FALSE", "AND FALSE FALSE");
//		「述語」とは、真理値を返す関数のことである。計算論において最も基本的な述語は
//		ISZERO で、これは引数がチャーチ数の 0であった場合には TRUE を、
//		そうでなければ FALSE を返す関数であり、以下のように定義できる。
		define(context, "ISZERO", "λn.n (λx.FALSE) TRUE");
		define(context, "0", "λf x.x");
		define(context, "1", "λf x.f x");
		define(context, "2", "λf x.f (f x)");
		define(context, "3", "λf x.f (f (f x))");
		equals(context, "TRUE", "ISZERO 0");
		equals(context, "FALSE", "ISZERO 1");
		equals(context, "FALSE", "ISZERO 2");
		equals(context, "FALSE", "ISZERO 3");
		equals(context, "TRUE", "ISZERO FALSE");
	}

}
