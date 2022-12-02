package com.jlox;
// Token is a class that represents a token.
class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    // Token is the constructor for the Token class.
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    // toString is a method that returns a string representation of the token.
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}