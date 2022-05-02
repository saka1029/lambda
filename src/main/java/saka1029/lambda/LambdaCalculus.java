package saka1029.lambda;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

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
                    return Lambda.of(variable, body);
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

    public static String string(Expression e) {
        return new Object() {
            StringBuilder sb = new StringBuilder();

            String string(Expression e) {
                if (e instanceof Lambda lambda) {
                    sb.append("λ").append(lambda.variable).append(".");
                    string(lambda.body);
                } else if (e instanceof Application a) {
                    boolean headParen = a.head instanceof Lambda;
                    boolean tailParen = !(a.tail instanceof Variable);
                    if (headParen)
                        sb.append("(");
                    string(a.head);
                    if (headParen)
                        sb.append(")");
                    sb.append(" ");
                    if (tailParen)
                        sb.append("(");
                    string(a.tail);
                    if (tailParen)
                        sb.append(")");
                } else
                    sb.append(e);
                return sb.toString();
            }
        }.string(e);
    }

    public static String normalize(Expression e) {
        return new Object() {
            StringBuilder sb = new StringBuilder();
            Binder<BoundVariable, String> binder = new Binder<>();
            int variableNumber = 0;

            String normalize(Expression e) {
                if (e instanceof Lambda l) {
                    String variableName = "%" + (variableNumber++);
                    sb.append("λ").append(variableName).append(".");
                    try (Unbind u = binder.bind(l.variable, variableName)) {
                        normalize(l.body);
                    }
                } else if (e instanceof Application a) {
                    boolean headParen = a.head instanceof Lambda;
                    boolean tailParen = !(a.tail instanceof Variable);
                    if (headParen)
                        sb.append("(");
                    normalize(a.head);
                    if (headParen)
                        sb.append(")");
                    sb.append(" ");
                    if (tailParen)
                        sb.append("(");
                    normalize(a.tail);
                    if (tailParen)
                        sb.append(")");
                } else if (e instanceof BoundVariable variable)
                    sb.append(binder.get(variable));
                else
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
}
