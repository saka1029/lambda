package saka1029.lambda;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;

public class TestEfficientInterpreter {
    
	static class LambdaTermException extends RuntimeException {
		public LambdaTermException(String format, Object... args) {
			super(format.formatted(args));
		}
	}
	
	static LambdaTermException error(String format, Object... args) {
		return new LambdaTermException(format, args);
	}

    static abstract class LambdaTerm {
    }
    
    static class Variable extends LambdaTerm {
        public final String name;

        private Variable(String name) {
            this.name = name;
        }
        
        private static final HashMap<String, Variable> ALL = new HashMap<>();

        public static Variable of(String name) {
            return ALL.computeIfAbsent(name, k -> new Variable(name));
        }

//        @Override
//        public int hashCode() {
//            return Objects.hash(name);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            return this == obj
//                || obj instanceof Variable v
//                	&& v.name.equals(name);
//        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    static class Application extends LambdaTerm {
        public final LambdaTerm function, argument;
        
        private Application(LambdaTerm function, LambdaTerm argument) {
            this.function = function;
            this.argument = argument;
        }
        
        public static Application of(LambdaTerm function, LambdaTerm argument) {
            return new Application(function, argument);
        }
        
        @Override
        public String toString() {
            return "(%s %s)".formatted(function, argument);
        }
        
    }
    
    static class Lambda extends LambdaTerm {
        public final Variable binder;
        public final LambdaTerm form;
        
        private Lambda(Variable binder, LambdaTerm form) {
            this.binder = binder;
            this.form = form;
        }
        
        @Override
        public String toString() {
            return "(lambda %s %s)".formatted(binder, form);
        }
    }

    public static Set<Variable> free_variables(LambdaTerm term) {
        Set<Variable> result = new HashSet<>();
        new Object() {
            void addFreeVariables(LambdaTerm t) {
                if (t instanceof Variable v)
                    result.add(v);
                else if (t instanceof Application a) {
                    addFreeVariables(a.function);
                    addFreeVariables(a.argument);
                } else if (t instanceof Lambda l) {
                    addFreeVariables(l.form);
                    result.remove(l.binder);
                }
            }
        }.addFreeVariables(term);
        return result;
    }
    
//    public static boolean is_free_in(Variable v, LambdaTerm t) {
//        return free_variables(t).contains(v);
//    }
    
    public static boolean is_free_in(Variable variable, LambdaTerm term) {
    	if (term instanceof Variable v)
    		return v.equals(variable);
    	else if (term instanceof Application a)
    		return is_free_in(variable, a.function)
    			|| is_free_in(variable, a.argument);
        else if (term instanceof Lambda l)
        	return !l.binder.equals(variable)
        		|| is_free_in(variable, l.form);
        else
        	throw error("Unknown type %s", term);
    }
    
}
