package com.jlox;

import java.util.List;
import com.jlox.Expr.Binary;
import com.jlox.Expr.Grouping;
import com.jlox.Expr.Literal;
import com.jlox.Expr.Unary;
import com.jlox.Expr.Variable;
import com.jlox.Stmt.Block;
import com.jlox.Stmt.Expression;
import com.jlox.Stmt.Print;
import com.jlox.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    // environment is a private variable because it is only used by the Interpreter
    private Environment environment = new Environment();

    // interpret method is the starting point of the interpreter
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                // execute each statement in the list
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    // Binary expressions are evaluated by evaluating the left and right operands
    // and then applying the operator to them.
    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");

            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);

        }
        // Unreachable.
        return null;
    }

    // Grouping expressions are evaluated by evaluating the expression inside the
    // parentheses and returning the result.
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    // Literal expressions return their value directly.
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    // Unary expressions return the result of applying the operator to the operand.
    @Override
    public Object visitUnaryExpr(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                checkNumberOperand(expr.operator, right);
                return !isTruthy(right);
        }
        // Unreachable.
        return null;
    }

    // Variable expressions return the value of the variable.
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // evaluate method is used to evaluate expressions
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // execute method is used to execute statements
    private void execute(Stmt stmt) {
        // stmt is a Stmt object, and accept is a method of the Stmt class
        stmt.accept(this);
    }

    // Block statements are executed by creating a new scope and executing each
    // statement in the block in that scope.
    void executeBlock(List<Stmt> statements,
            Environment environment) {
        // save the current environment
        Environment previous = this.environment;
        try {
            // set the current environment to the new environment
            this.environment = environment;
            // execute each statement in the block
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // restore the previous environment
            this.environment = previous;
        }
    }

    // isTruthy method is used to determine if an object is truthy or not
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    // isEqual method is used to determine if two objects are equal
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    // checkNumberOperand method is used to check if an operand is a number
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    // checkNumberOperands method is used to check if two operands are numbers
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // stringify method is used to convert an object to a string
    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            // remove the decimal point if it is a whole number
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    // visitPrintStmt method is used to evaluate the expression and print the result
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    // visitVarStmt method is used to evaluate the initializer and define the
    // variable
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    // visitVariableExpr method is used to look up the variable in the environment
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    // visitBlockStmt method is used to execute the block in a new scope
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

}