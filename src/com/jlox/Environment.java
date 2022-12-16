package com.jlox;

import java.util.HashMap;
import java.util.Map;

// Environment class is a subclass of Stmt to represent variable declarations like var a = 1;
class Environment {
    // some notes about the Environment class
    // if the enclosing environment is null, it means that the current environment is the global
    // else it means that the current environment is a local environment
    // we need to keep track of the enclosing environment to implement variable scoping
    // we can access the enclosing environment using the enclosing field
    // we can have a local environment inside a local environment, so we need to keep track of the enclosing environment 
    final Environment enclosing;

    // constructor for the Environment class
    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    // Map is a class that maps keys to values
    private final Map<String, Object> values = new HashMap<>();

    // get method is a method to get the value of a variable
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        // if the variable is not found in the current environment, check the enclosing
        // environment
        if (enclosing != null)
            return enclosing.get(name);
        // if the variable is not found in the enclosing environment, throw an error
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    // assign method is a method to assign a value to a variable
    void assign(Token name, Object value) {
        // if the variable is found in the current environment, assign the value to the
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        // if the variable is not found in the current environment, check the enclosing
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        // if the variable is not found in the enclosing environment, throw an error
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    // define method is a method to define a variable
    void define(String name, Object value) {
        values.put(name, value);
    }
}