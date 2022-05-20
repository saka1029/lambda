package saka1029.lambda;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class LambdaCalculus {

    private LambdaCalculus() {}

    public static Expression parse(String source) {
        return parse(new StringReader(source));
    }

    /**
     * コードポイントを返すReaderのフィルターです。
     * read()はUTF-16の1文字ではなく、コードポイントとしての1文字を返します。
     */
    static class CodePointReader {

        Reader in;

        public CodePointReader(String source) {
            this.in = new StringReader(source);
        }

        public CodePointReader(Reader in) {
            this.in = in;
        }

        public int read() throws IOException {
            int first = in.read();
            if (first == -1)
                return -1;
            if (!Character.isHighSurrogate((char)first))
                return first;
            int second = in.read();
            if (second == -1)
                throw new IOException("low surrogate expected after %d".formatted(first));
            if (!Character.isLowSurrogate((char)second))
                throw new IOException("invalid surrogate pair (%d, %d)".formatted(first, second));
            return Character.toCodePoint((char)first, (char)second);
        }
    }

    public static Expression parse(Reader reader) {
        return new Object() {
            Binder<String, BoundVariable> binder = new Binder<>();
            CodePointReader cpreader = new CodePointReader(reader);
            int ch = get();

            int get() {
                try {
                    return ch = cpreader.read();
                } catch (IOException e) {
                    throw new LambdaCalculusException(e);
                }
            }

            void spaces() {
                while (Character.isWhitespace(ch))
                    get();
            }

            static boolean isVariable(int ch) {
                switch (ch) {
                    case 'λ':
                    case '\\':
                    case -1:
                    case '.':
                    case '(':
                    case ')':
                        return false;
                    default:
                        return !Character.isWhitespace(ch);
                }
            }

            String variableName() {
                StringBuilder sb = new StringBuilder();
                while (isVariable(ch)) {
                    sb.appendCodePoint(ch);
                    get();
                }
                return sb.toString();
            }

            Lambda lambda() {
                spaces();
                if (!isVariable(ch))
                    throw new LambdaCalculusException("variable expected but '%s'",
                        Character.toString(ch));
                String name = variableName();
                BoundVariable variable = BoundVariable.of(name);
                try (Unbind u = binder.bind(name, variable)) {
                    Expression body;
                    if (ch == '.') {
                        get(); // skip '.'
                        body = expression();
                    } else
                        body = lambda(); // lambda cascading
                    return Lambda.of(variable, body, binder.refCount(name));
                }
            }

            Expression paren() {
                spaces(); // skip spaces after '('
                Expression e = expression();
                spaces(); // skip spaces before ')'
                if (ch != ')')
                    throw new LambdaCalculusException("')' expected");
                get(); // skip ')'
                return e;
            }

            Variable variable() {
                String name = variableName();
                BoundVariable variable = binder.get(name);
                if (variable == null)
                    return FreeVariable.of(name);
                else
                    return variable;
            }

            Expression term() {
                spaces();
                switch (ch) {
                    case -1:
                        throw new LambdaCalculusException("unexpected EOS");
                    case 'λ':
                    case '\\':
                        get(); // skip 'λ' or '\\'
                        return lambda();
                    case '(':
                        get(); // skip '('
                        return paren();
                    default:
                        if (!isVariable(ch))
                            throw new LambdaCalculusException("unexpected char '%s'",
                                Character.toString(ch));
                        return variable();
                }

            }

            Expression expression() {
                Expression term = term();
                while (true) {
                    spaces();
                    if (ch != '(' && ch != 'λ' && ch != '\\' && !isVariable(ch))
                        break;
                    term = Application.of(term, term());
                }
                return term;
            }

            Expression parse() {
                Expression e = expression();
                if (ch != -1)
                    throw new LambdaCalculusException("extra string");
                return e;
            }
        }.parse();
    }

    // public static int NORMALIZED_VAR_NAME_BASE = 'ⓐ';
    public static int NORMALIZED_VAR_NAME_BASE = 'a';
/**
 * 
 * ⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ
 */
    public static String normalize(Expression e) {
        return new Object() {
            StringBuilder sb = new StringBuilder();
            Binder<BoundVariable, String> binder = new Binder<>();
            int variableNumber = 0;

            void paren(Expression e, boolean paren) {
            	if (paren)
            		sb.append("(");
            	normalize(e);
            	if (paren)
            		sb.append(")");
            }

            String normalize(Expression e) {
                if (e instanceof Lambda l) {
                    String variableName = String.valueOf(
                        (char)(NORMALIZED_VAR_NAME_BASE + variableNumber++));
                    sb.append("\\").append(variableName).append(".");
                    try (Unbind u = binder.bind(l.variable, variableName)) {
                        normalize(l.body);
                    }
                    --variableNumber;   // スコープを外れたら番号をリセットします。
                } else if (e instanceof Application a) {
                	paren(a.head, a.head instanceof Lambda);
                    sb.append(" ");
                	paren(a.tail, !(a.tail instanceof Variable));
                } else if (e instanceof BoundVariable v) {
                    String x = binder.get(v);
                    if (x == null)
                        throw new LambdaCalculusException("unknown bound variable: %s", v);
                    sb.append(x);
                } else
                    sb.append(e);
                return sb.toString();
            }
        }.normalize(e);
    }
 
    public static String tree(Expression e) {
        try (StringWriter sw = new StringWriter();
            PrintWriter w = new PrintWriter(sw)) {
            new Object() {
                void tree(Expression e, int level) {
                    w.print("    ".repeat(level));
                    if (e instanceof Lambda l) {
                        w.printf("lambda %s%n", l.variable);
                        tree(l.body, level + 1);
                    } else if (e instanceof Application a) {
                        w.printf("apply%n");
                        tree(a.head, level + 1);
                        tree(a.tail, level + 1);
                    } else
                        w.println(e);
                }
            }.tree(e, 0);
            return sw.toString();
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    public static Expression reduce(Expression e, Map<FreeVariable, Expression> freeVariables) {
        var obj = new Object() {
            Binder<BoundVariable, Expression> boundVariables = new Binder<>();

            int n = 0;

            String indent(int n) {
                return "  ".repeat(n);
            }

            Expression replace(Expression e) {
            	Expression r;
            	if (e instanceof FreeVariable f) {
            		Expression v = freeVariables.get(f);
            		r = v == null ? f : v;
            	} else if (e instanceof BoundVariable b) {
            		Expression v = boundVariables.get(b);
            		if (v == null)
                        throw new LambdaCalculusException("undefined bound varialbe %s in %s", b, boundVariables);
            		r = v;
            	} else if (e instanceof Lambda l) {
            		BoundVariable n = BoundVariable.of(l.variable.name);
            		try (Unbind u = boundVariables.bind(l.variable, n)) {
            			r = Lambda.of(n, replace(l.body), boundVariables.refCount(n));
            		}
            	} else if (e instanceof Application a) {
            		r = Application.of(replace(a.head), replace(a.tail));
            	} else
                    throw new LambdaCalculusException("unknown expression: %s", e);
            	return r;
            }

            Expression reduce(Expression e) {
                System.out.printf("%s< %s %s%n", indent(n), e, boundVariables);
                ++n;
                Expression r;
                if (e instanceof FreeVariable f) {
                    Expression v = freeVariables.get(f);
                    return v == null ? f : reduce(v);
                } else if (e instanceof BoundVariable b) {
                    Expression v = boundVariables.get(b);
                    if (v == null)
                        throw new LambdaCalculusException("undefined bound varialbe %s in %s", b, boundVariables);
                    r = v;
                } else if (e instanceof Lambda l) {
                    BoundVariable v = BoundVariable.of(l.variable.name);
                    try (Unbind u = boundVariables.bind(l.variable, v)) {
                        r = Lambda.of(v, reduce(l.body), boundVariables.refCount(l.variable));
                    }
                } else if (e instanceof Application a) {
                	Expression h = reduce(a.head);
                	Expression t = reduce(a.tail);
                	if (h instanceof Lambda l)
                		try (Unbind u = boundVariables.bind(l.variable,  t)) {
							r = reduce(l.body);
                		}
                	else
                		r = Application.of(h, t);
                } else
                    throw new LambdaCalculusException("unknown expression: %s", e);
                --n;
                System.out.printf("%s> %s%n", indent(n), r);
                return r;
            }
        };
        return obj.reduce(e);
    }

    public static Expression reduce(Expression e) {
        return reduce(e, new HashMap<>());
    }
}
