package saka1029.lambda;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LambdaCalculus {

    private LambdaCalculus() {}

    public static LambdaCalculusException error(String format, Object... args) {
    	return new LambdaCalculusException(format, args);
    }

    public static LambdaCalculusException error(Throwable cause) {
    	return new LambdaCalculusException(cause);
    }

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
                    throw error(e);
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
                    throw error("variable expected but '%s'", Character.toString(ch));
                String name = variableName();
                BoundVariable variable = BoundVariable.of(name);
                return binder.bind(name, variable, () -> {
                    Expression body;
                    if (ch == '.') {
                        get(); // skip '.'
                        body = expression();
                    } else
                        body = lambda(); // lambda cascading
                    return Lambda.of(variable, body, binder.refCount(name));
                });
            }

            Expression paren() {
                spaces(); // skip spaces after '('
                Expression e = expression();
                spaces(); // skip spaces before ')'
                if (ch != ')')
                    throw error("')' expected");
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
                        throw error("unexpected EOS");
                    case 'λ':
                    case '\\':
                        get(); // skip 'λ' or '\\'
                        return lambda();
                    case '(':
                        get(); // skip '('
                        return paren();
                    default:
                        if (!isVariable(ch))
                            throw error("unexpected char '%s'",
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
                    throw error("extra string");
                return e;
            }
        }.parse();
    }
    
    public static String stringDetail(Expression e) {
    	return string(e, true);
    }

    public static String string(Expression e) {
    	return string(e, false);
    }

    public static String string(Expression e, boolean detailBoundVariable) {
		StringBuilder sb = new StringBuilder();
    	new Object() {

            void paren(Expression e, boolean paren) {
            	if (paren)
            		sb.append("(");
            	process(e);
            	if (paren)
            		sb.append(")");
            }

            void process(Expression e) {
    			if (e instanceof FreeVariable f)
					sb.append(f.name);
    			else if (e instanceof BoundVariable b) {
					sb.append(b.name);
					if (detailBoundVariable)
						sb.append("_").append(b.id);
    			} else if (e instanceof Lambda l) {
    				String sep = "λ";
    				Expression body = l;
    				for ( ; body instanceof Lambda ll; body = ll.body, sep = " ") {
    					sb.append(sep);
    					process(ll.variable);
    				}
			        sb.append(".");
			        process(body);
    			} else if (e instanceof Application a) {
                	paren(a.head, a.head instanceof Lambda);
                    sb.append(" ");
                	paren(a.tail, !(a.tail instanceof Variable));
    			} else
    				throw error("unknown expression: %s", e);
    		}
    	}.process(e);
    	return sb.toString();
    }

    // public static int NORMALIZED_VAR_NAME_BASE = 'ⓐ';
    public static int NORMALIZED_VAR_NAME_BASE = 'a';
/**
 * 
 * ⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛⓜⓝⓞⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ
 */
    public static String normalize(Expression e) {
		StringBuilder sb = new StringBuilder();
        new Object() {
            Binder<BoundVariable, String> binder = new Binder<>();
            int seq = 0;

            void paren(Expression e, boolean paren) {
            	if (paren)
            		sb.append("(");
            	process(e);
            	if (paren)
            		sb.append(")");
            }

            void lambda(Expression e, String sep) {
            	if (e instanceof Lambda l) {
					String name = String.valueOf((char)(NORMALIZED_VAR_NAME_BASE + seq++));
					sb.append(sep).append(name);
					binder.bind(l.variable, name, () -> {
						lambda(l.body, " ");
						return null;
					});
					--seq;
            	} else {
            		sb.append(".");
            		process(e);
            	}
            }

            void process(Expression e) {
            	if (e instanceof FreeVariable f)
            		sb.append(f.name);
            	else if (e instanceof BoundVariable b) {
            		String x = binder.get(b);
            		if (x == null)
                        throw error("unknown bound variable: %s", b);
            		sb.append(x);
            	} else if (e instanceof Lambda l) {
            		lambda(l, "λ");
                } else if (e instanceof Application a) {
                	paren(a.head, a.head instanceof Lambda);
                    sb.append(" ");
                	paren(a.tail, !(a.tail instanceof Variable));
                } else
    				throw error("unknown expression: %s", e);
            }
        }.process(e);
        return sb.toString();
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

    public static Expression expand(Expression e, Map<FreeVariable, Expression> context) {
    	return new Object() {
    		Binder<BoundVariable, BoundVariable> binder = new Binder<>();
    		
    		Expression process(Expression e) {
    			if (e instanceof FreeVariable f) {
    				Expression n = context.get(f);
    				return n == null ? f : process(n);
    			} else if (e instanceof BoundVariable b) {
    				BoundVariable n = binder.get(b);
    				if (n == null)
    					throw error("undefine bound variable: %s", b);
    				return n;
    			} else if (e instanceof Lambda l) {
    				BoundVariable o = l.variable;
    				BoundVariable n = BoundVariable.of(l.variable.name);
    				return binder.bind(o, n,
    					() -> Lambda.of(n, process(l.body), binder.refCount(o)));
    			} else if (e instanceof Application a) {
    				return Application.of(process(a.head), process(a.tail));
    			} else
    				throw error("unknown expression: %s", e);
    		}
    	}.process(e);
    }

//    public static Expression reduce(Expression e, Map<FreeVariable, Expression> context) {
//        var obj = new Object() {
//            Binder<BoundVariable, Expression> binding = new Binder<>();
//
//            int n = 0;
//
//            String indent(int n) {
//                return "  ".repeat(n);
//            }
//
//            Expression replace(Expression e) {
//            	Expression r;
//            	if (e instanceof FreeVariable f) {
//            		Expression v = context.get(f);
//            		r = v == null ? f : v;
//            	} else if (e instanceof BoundVariable b) {
//            		Expression v = binding.get(b);
//            		r = v == null ? b : v;
//            	} else if (e instanceof Lambda l) {
//            		BoundVariable n = BoundVariable.of(l.variable.name);
//            		r = binding.bind(l.variable, n,
//            			() -> Lambda.of(n, replace(l.body), binding.refCount(n)));
//            	} else if (e instanceof Application a) {
//            		r = Application.of(replace(a.head), replace(a.tail));
//            	} else
//                    throw error("unknown expression: %s", e);
//            	return r;
//            }
//
//            Expression reduce(Expression e) {
////                System.out.printf("%s< %s %s%n", indent(n), e, binding);
//                ++n;
//                Expression r;
//                if (e instanceof FreeVariable f) {
//                    Expression v = context.get(f);
//                    r = v == null ? f : v;
//                } else if (e instanceof BoundVariable b) {
//                    Expression v = binding.get(b);
//                    r = v == null ? b : v;
//                } else if (e instanceof Lambda l) {
//                	if (l.refCount == 0)
//                		// η-変換 (eta-conversion)
//                		r = reduce(l.body);	
//                	else {
//                		// α-変換 (alpha-conversion)
//                		BoundVariable oldVar = l.variable;
//                        BoundVariable newVar = BoundVariable.of(l.variable.name);
//                        r = binding.bind(oldVar, newVar,
//                            () -> {
//                            	Expression newBody = reduce(l.body);
//                            	return Lambda.of(newVar, newBody, binding.refCount(oldVar));
//                            });
//                	}
//                } else if (e instanceof Application a) {
//                	Expression h = reduce(a.head);
//                	Expression t = reduce(a.tail);
//                	if (h instanceof Lambda l)
//                		// β-簡約 (beta-conversion)
//                		r = binding.bind(l.variable, t, () -> reduce(l.body));
//                	else
//                		r = Application.of(h, t);
//                } else
//                    throw error("unknown expression: %s", e);
//                --n;
////                System.out.printf("%s> %s%n", indent(n), r);
//                return r;
//            }
//        };
//        return obj.reduce(e);
//    }

    public static Expression reduce(Expression e, Map<FreeVariable, Expression> context) {
    	return reduce(expand(e, context));
    }

    public static Expression reduce(Expression e) {
    	var obj = new Object() {
    		Binder<BoundVariable, Expression> binder = new Binder<>();

    		Expression process(Expression e) {
    			Expression result;
    			if (e instanceof FreeVariable f) {
    				result = f;
    			} else if (e instanceof BoundVariable b) {
    				Expression x = binder.get(b);
    				result = x == null ? b : x;
    			} else if (e instanceof Lambda l) {
//    				Expression x = l.etaConversion();
//    				if (x != null)
//    					result = process(x);
//    				else {
                        BoundVariable o = l.variable;
                        BoundVariable n = BoundVariable.of(o.name);
                        result = binder.bind(o, n,
                            () -> Lambda.of(n, process(l.body), binder.refCount(o)));
//    				}
    			} else if (e instanceof Application a) {
    				Expression head = process(a.head);
    				Expression tail = process(a.tail);
    				if (head instanceof Lambda l)
    					result = binder.bind(l.variable, tail,
    						() -> process(l.body));
    				else
    					result = Application.of(head, tail);
    			} else
                    throw error("unknown expression: %s", e);
    			return result;
    		}
    	};
    	return obj.process(e);
    }
    
    public static boolean same(Expression left, Expression right) {
    	Objects.requireNonNull(left);
    	Objects.requireNonNull(right);
    	if (left == right)
    		return true;
    	var obj =  new Object() {
    		Binder<BoundVariable, BoundVariable> binder = new Binder<>();

    		boolean test(Expression left, Expression right) {
    			if (left instanceof FreeVariable)
    				return left.equals(right);
    			else if (left instanceof BoundVariable l)
    				return right instanceof BoundVariable r
    					&& r.equals(binder.get(l));
    			else if (left instanceof Lambda l)
    				return right instanceof Lambda r
    					&& binder.bind(l.variable, r.variable,
    						() -> (boolean)test(l.body, r.body));
    			else if (left instanceof Application l)
    				return right instanceof Application r
    					&& test(l.head, r.head)
    					&& test(l.tail, r.tail);
    			else
                    throw error("unknown expression: %s", left);
    		}
    	};
    	return obj.test(left, right);
    }
}
