package com.jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static com.jlox.TokenType.*;

// *********************************************************************
// don't trace the code in this class because it has recursion functions
// *********************************************************************
class Parser {
    // create a list of tokens
    private final List<Token> tokens;
    private int current = 0;

    // RuntimeException : A RuntimeException is an unchecked exception.
    static class ParseError extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    // constructor for Parser
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // start parsing the tokens
    List<Stmt> parse() {
        // create a list of statements
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            // recursively parse the tokens #NOTE : if you don't understand this it's okay
            statements.add(declaration());
        }
        return statements;
    }

    // expression : assignment ;
    private Expr expression() {
        return assignment();
    }

    // declaration : varDeclaration | statement ;
    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError error) {
            // if there is an error, synchronize the parser
            synchronize();
            return null;
        }
    }

    // statement : printStatement | expressionStatement | block ;
    private Stmt statement() {
        if (match(IF))
            return ifStatement();
        if (match(FOR))
            return forStatement();
        if (match(WHILE))
            return whileStatement();
        if (match(PRINT))
            return printStatement();
        if (match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    // for statement : "for" "(" ( varDeclaration | expressionStatement | ";" )
    // expression? ";" expression? ")" statement ;
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        // if the next token is a semicolon, then the initializer is null
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        // if the next token is a semicolon, then the condition is null
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();
        // if the increment is not null, then add it to the body
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }
        // if the condition is null, then set it to true
        if (condition == null)
            condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);
        // if the initializer is not null, then add it to the body
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    // whileStatement : "while" "(" expression ")" statement ;
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    // ifStatement : "if" "(" expression ")" statement ( "else" statement )? ;
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // printStatement : "print" expression ";" ;
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    // varDeclaration : "var" IDENTIFIER ( "=" expression )? ";" ;
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // expressionStatement : expression ";" ;
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // block : "{" declaration* "}" ;
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    // assignment : ( call "." )? IDENTIFIER "=" assignment | logic_or ;
    private Expr assignment() {

        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    // or : and ( "or" and )* ;
    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // and : equality ( "and" equality )* ;
    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // equality : comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // comparison : addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private Expr comparison() {
        Expr expr = addition();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // addition : multiplication ( ( "-" | "+" ) multiplication )* ;
    private Expr addition() {
        Expr expr = multiplication();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // multiplication : unary ( ( "/" | "*" ) unary )* ;
    private Expr multiplication() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // unary : ( "!" | "-" ) unary | call ;
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    // primary : NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // match : to check if the current token is one of the given types
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // consume : to consume the current token if it matches the given type
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        // if the current token does not match the given type, report an error
        throw error(peek(), message);
    }

    // check : to check if the current token is of the given type
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    // advance : to advance the scanner to the next token
    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    // isAtEnd : to check if the scanner has reached the end of the input
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // peek : to get the current token
    private Token peek() {
        return tokens.get(current);
    }

    // previous : to get the previous token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // error : to report an error at the given token
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // synchronize : to recover from a parsing error
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

}