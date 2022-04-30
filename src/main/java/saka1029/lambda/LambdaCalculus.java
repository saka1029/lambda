package saka1029.lambda;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.lang.model.util.ElementScanner14;

public class LambdaCalculus {

    private LambdaCalculus() {}

    public static Expression parse(String source) {
        return parse(new StringReader(source));
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

    public static String toNormalizedString(Expression e) {
        return new Object() {
            StringBuilder sb = new StringBuilder();
            Binder<BoundVariable, String> binder = new Binder<>();
            int variableNumber = 0;

            String normalize(Expression e) {
                if (e instanceof Lambda lambda) {
                    String variableName = "%" + (variableNumber++);
                    sb.append("λ").append(variableName).append(".");
                    try (Unbind u = binder.bind(lambda.variable, variableName)) {
                        normalize(lambda.body);
                    }
                } else if (e instanceof Application application) {
                    boolean headParen = application.head instanceof Lambda;
                    boolean tailParen = !(application.tail instanceof Variable);
                    if (headParen)
                        sb.append("(");
                    normalize(application.head);
                    if (headParen)
                        sb.append(")");
                    sb.append(" ");
                    if (tailParen)
                        sb.append("(");
                    normalize(application.tail);
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
}
