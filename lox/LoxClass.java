package lox;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name,LoxClass superclass, Map<String, LoxFunction> methods){
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    LoxFunction findMethod(String name){
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        if (superclass != null) {
            return superclass.findMethod(name);
        }
        return null;
    }

    @Override
    public String toString(){
        return name;
    }
    // Implementing LoxCallable
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity(){
        LoxFunction initialize = findMethod("init");
        if (initialize == null) {
            return 0;
        }
        return initialize.arity();
    }
}
