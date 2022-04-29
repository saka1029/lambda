package saka1029.lambda;

import java.io.IOException;
import saka1029.io.CodePointReader;

public class LambdaCalculus {
    
    private LambdaCalculus() {}

    public static Expression parse(String source) {
        return parse(new CodePointReader(source));
    }

    public static Expression parse(CodePointReader reader) {
        return new Object() {
            int ch = get();

            int get() {
                try {
                    return ch = reader.read();
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
                case 'λ': case '\\':
                case -1: case '.': case '(': case ')':
                    return false;
                default:
                    return !Character.isWhitespace(ch);
                }
            }

            String variableName() {
                StringBuilder sb = new StringBuilder();
                while (isVariable(ch)) {
                    sb.append((char)ch);
                    get();
                }
                return sb.toString();
            }

            Lambda lambda(Bind<String, BoundVariable> bind) {
                spaces();
                if (!isVariable(ch))
                    throw new LambdaCalculusException("variable expected but '%c'", (char)ch);
                String name = variableName();
                BoundVariable variable = BoundVariable.of(name);
                Bind<String, BoundVariable> newBind = Bind.put(bind, name, variable);
                Expression body;
                if (ch == '.') {
                    get();      // skip '.'
                    body = expression(newBind);
                } else
                    body = lambda(newBind);     // lambda cascading
                return Lambda.of(variable, body);
            }

            Expression paren(Bind<String, BoundVariable> bind) {
                spaces();   // skip spaces after '('
                Expression e = expression(bind);
                spaces();   // skip spaces before ')'
                if (ch != ')')
                    throw new LambdaCalculusException("')' expected");
                get();      // skip ')'
                return e;
            }

            Variable variable(Bind<String, BoundVariable> bind) {
                String name = variableName();
                BoundVariable variable = Bind.get(bind, name);
                if (variable == null)
                    return FreeVariable.of(name);
                else
                    return variable;
            }

            Expression term(Bind<String, BoundVariable> bind) {
                spaces();
                switch (ch) {
                case -1:
                    throw new LambdaCalculusException("unexpected EOS");
                case 'λ': case '\\':
                    get();      // skip 'λ' or '\\'
                    return lambda(bind);
                case '(':
                    get();      // skip '('
                    return paren(bind);
                default:
                    if (!isVariable(ch))
                        throw new LambdaCalculusException("unexpected char '%c'", (char)ch);
                    return variable(bind);
                }

            }

            Expression expression(Bind<String, BoundVariable> bind) {
                Expression term = term(bind);
                while (true) {
                    spaces();
                    if (ch != '(' && ch != 'λ' && ch != '\\' && !isVariable(ch))
                        break;
                    term = Application.of(term, term(bind));
                }
                return term;
            }

            Expression parse() {
                Expression e = expression(null);
                if (ch != -1)
                    throw new LambdaCalculusException("extra string");
                return e;
            }
        }.parse();
    }
}
