package com.jlox;

import java.util.List;
// IOException is a checked exception, so we need to catch it or declare it
import java.io.IOException;
// BufferedReader is a class that reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
import java.io.BufferedReader;
// InputStreamReader is a bridge from byte streams to character streams: It reads bytes and decodes them into characters using a specified charset.
import java.io.InputStreamReader;
// charset.Charset is a named mapping between sequences of sixteen-bit Unicode code units and sequences of bytes.
import java.nio.charset.Charset;
// file.Files is a utility class for working with files.
import java.nio.file.Files;
// file.Paths is a utility class for working with file paths.
import java.nio.file.Paths;

public class Lox {
    // hadErrors is a static variable because it is shared by all instances of the
    // Lox class.
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    // interpreter is a static variable because it is shared by all instances of the
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            // if the user passes in more than one argument, print an error message and
            // exit.
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            // if the user passes in one argument, run the file.
            runFile(args[0]);
        } else {
            // if the user passes in no arguments, run the REPL.
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        // read the file into a byte array.
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        // convert the byte array to a string and run it.
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code.
        if (hadError)
            // if hadError is true, exit with code 65.
            System.exit(65);

    }

    private static void runPrompt() throws IOException {
        // create a new InputStreamReader that reads from System.in.
        InputStreamReader input = new InputStreamReader(System.in);
        // create a new BufferedReader that reads from the InputStreamReader.
        BufferedReader reader = new BufferedReader(input);
        // create a new StringBuilder that will hold the user's input.
        for (;;) {
            // print a prompt.
            System.out.print("> ");
            // read a line from the user.
            run(reader.readLine());
            // reset hadError to false.
            hadError = false;
        }
    }

    private static void run(String source) {
        // create a new Scanner that will tokenize the source code.
        Scanner scanner = new Scanner(source);
        // create a new List of Tokens.
        List<Token> tokens = scanner.scanTokens();

        // create a new Parser that will parse the tokens.
        Parser parser = new Parser(tokens);
        // create a new Expr that will hold the AST.
        List<Stmt> statements = parser.parse();
        // stop if there was a syntax error.
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);

        interpreter.interpret(statements);

    }

    // parser error.
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    // scanner error.
    static void error(int line, String message) {
        // print the error message.
        report(line, "", message);
    }

    // report is a helper function that prints an error message.
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
    // runtime error.
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
