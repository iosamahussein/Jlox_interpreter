package com.jlox;

import com.jlox.Expr.Assign;
import com.jlox.Expr.Logical;
import com.jlox.Expr.Variable;
// AstPrinter class is a subclass of Expr.Visitor<String> to print the AST
// AST is an abstract syntax tree
class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        // TODO Auto-generated method
        return null;
       
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        // TODO Auto-generated method
        return null;
    }
    @Override
    public String visitLogicalExpr(Logical expr) {
        // TODO Auto-generated method stub
        return null;
    }

}