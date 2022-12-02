package com.jlox;

// some imports that we need.
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Scanner is a class that scans the source code and returns a list of tokens.
public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    // start and current are the indices of the first and last characters of the substring
    private int start = 0;
    private int current = 0;
    // line is the line number of the current token.
    private int line = 1;
    // keywords is a map of keywords to their token types.
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }
    // Scanner is the constructor for the Scanner class.
    Scanner(String source) {
        this.source = source;
    }
    // scanTokens is a method that returns a list of tokens.
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        // add the EOF token to the list of tokens.
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
    // isAtEnd is a method that returns true if we have reached the end of the source code.
    private boolean isAtEnd() {
        return current >= source.length();
    }
    // scanToken is a method that scans a single token.
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }
    // addToken is a method that adds a token to the list of tokens.
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    // addToken is a method that adds a token to the list of tokens.
    private void addToken(TokenType type, Object literal) {
        // get the substring from start to current.
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
    // match is a method that checks if the current character is equal to the expected
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;
        // if the current character is equal to the expected character, advance the current
        current++;
        return true;
    }
    // peek is a method that returns the current character.
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }
    // peekNext is a method that returns the next character.
    private char peekNext() {
        // if the current character is the last character, return null.
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }
    // string is a method that scans a string.
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // if the current character is a newline, increment the line number.
            if (peek() == '\n')
                line++;
            advance();
        }
        // Unterminated string.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }
        // The closing ".
        advance();
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);

    }
    // isAlpha is a method that returns true if the character is a letter.
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    // isDigit is a method that returns true if the character is a digit.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    // number is a method that scans a number.
    private void number() {
        while (isDigit(peek()))
            advance();
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek()))
                advance();
        }
        // we want to convert the string to a double.
        addToken(TokenType.NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }
    // identifier is a method that scans an identifier.
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();
        // See if the identifier is a reserved word.
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        // if the identifier is not a reserved word, set the type to IDENTIFIER.
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }
    // isAlphaNumeric is a method that returns true if the character is a letter or digit.
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    // scanToken is a method that scans a single token.
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }
}
