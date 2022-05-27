package saka1029.lambda;

public class Lambda extends Expression {

    public final BoundVariable variable;
    public final int refCount;
    public final Expression body;

    private Lambda(BoundVariable variable, Expression body, int refCount) {
        this.variable = variable;
        this.refCount = refCount;
        this.body = body;
    }

    public static Lambda of(BoundVariable variable, Expression body, int refCount) {
        return new Lambda(variable, body, refCount);
    }
    
    /**
     * η変換可能であれば、その結果を返します。
     * 不可能な場合はnullを返します。
     * 
     * η変換は2つの関数が全ての引数に対して常に同じ値を返すようなとき、互いに同値であるとみなすという概念である。
     * <code>
     * λV.E V →η E
     * </code>
     * ただし、EにVが自由出現しないときに限る。
     */
    public Expression etaConversion() {
    	if (refCount == 1 && body instanceof Application a && a.tail.equals(variable))
    		return a.head;
    	return null;
    }
}
