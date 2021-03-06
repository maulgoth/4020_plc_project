package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LexerTests_JERUPDATE {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String test, String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Alphabetic", "getName", true),
                Arguments.of("Alphanumeric", "thelegend27", true),
                Arguments.of("Leading Hyphen", "-five", false),
                Arguments.of("Leading Digit", "1fish2fish3fishbluefish", false),
                Arguments.of("Symbol in Middle", "getN$$ame", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String test, String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Decimal", "123.456", false),
                Arguments.of("Signed Decimal", "-1.0", false),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String test, String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                Arguments.of("Integer", "1", false),
                Arguments.of("Multiple Digits", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCharacter(String test, String input, boolean success) {
        test(input, Token.Type.CHARACTER, success);
    }

    private static Stream<Arguments> testCharacter() {
        return Stream.of(
                Arguments.of("Alphabetic", "\'c\'", true),
                Arguments.of("Newline Escape", "\'\\n\'", true),
                Arguments.of("Empty", "\'\'", false),
                Arguments.of("Multiple", "\'abc\'", false),
                Arguments.of("No close", "\'a", false),
                Arguments.of("Escape single quote", "\'\\\'\'", true),
                Arguments.of("No ''' allowed", "\'\'\'", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String test, String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Alphabetic", "\"abc\"", true),
                Arguments.of("Newline Escape", "\"Hello,\\nWorld\"", true),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                Arguments.of("Start without Quote", "notGood\"", false),
                Arguments.of("Cannot escape \" double Quote", "\" This is not \" allowed!\"", false),
                Arguments.of("Special escapes", "\"sq\\\'dq\\\"bs\\\\\"", true),
                Arguments.of("NEWLINE", "\"This is a \\n test\"", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String test, String input, boolean success) {
        //this test requires our lex() method, since that's where whitespace is handled.
        test(input, Arrays.asList(new Token(Token.Type.OPERATOR, input, 0)), success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("Character", "(", true),
                Arguments.of("Comparison", "<=", true),
                Arguments.of("Space", " ", false),
                Arguments.of("Tab", "\t", false),
                Arguments.of("Dollar Sign", "$", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExamples(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testExamples() {
        return Stream.of(
                // Arguments.of("Example 1", "LET x = 5;", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "LET", 0),
                //         new Token(Token.Type.IDENTIFIER, "x", 4),
                //         new Token(Token.Type.OPERATOR, "=", 6),
                //         new Token(Token.Type.INTEGER, "5", 8),
                //         new Token(Token.Type.OPERATOR, ";", 9)
                // )),
                // Arguments.of("Example 2", "print(\"Hello, World!\");", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "print", 0),
                //         new Token(Token.Type.OPERATOR, "(", 5),
                //         new Token(Token.Type.STRING, "\"Hello, World!\"", 6),
                //         new Token(Token.Type.OPERATOR, ")", 21),
                //         new Token(Token.Type.OPERATOR, ";", 22)
                // )),
                // Arguments.of("Example 3", "let x <= (5 * x + 14.753);", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "let", 0),
                //         new Token(Token.Type.IDENTIFIER, "x", 4),
                //         new Token(Token.Type.OPERATOR, "<=", 6),
                //         new Token(Token.Type.OPERATOR, "(", 9),
                //         new Token(Token.Type.INTEGER, "5", 10),
                //         new Token(Token.Type.OPERATOR, "*", 12),
                //         new Token(Token.Type.IDENTIFIER, "x", 14),
                //         new Token(Token.Type.OPERATOR, "+", 16),
                //         new Token(Token.Type.DECIMAL, "14.753", 18),
                //         new Token(Token.Type.OPERATOR, ")", 24),
                //         new Token(Token.Type.OPERATOR, ";", 25)
                // )),
                // Arguments.of("Windows EOL", "one␍␊two", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "one", 0),
                //         new Token(Token.Type.IDENTIFIER, "two", 5)
                // )),
                // Arguments.of("Mixed Whitespace", "one ␈␊␍␉two", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "one", 0),
                //         new Token(Token.Type.IDENTIFIER, "two", 8)
                // )),
                // Arguments.of("All types", "abc 123 456.789 \'c\' \"string\" %", Arrays.asList(
                //         new Token(Token.Type.IDENTIFIER, "abc", 0),
                //         new Token(Token.Type.INTEGER, "123", 4),
                //         new Token(Token.Type.DECIMAL, "456.789", 8),
                //         new Token(Token.Type.CHARACTER, "\'c\'", 16),
                //         new Token(Token.Type.STRING, "\"string\"", 20),
                //         new Token(Token.Type.OPERATOR, "%", 29)
                // )),

                // FIZZ BUZZ ////////////////////////////////////////////////////////////////////////////////////////////////////////
                Arguments.of("Fizzbuzz", "LET i = 1;␍␊WHILE i <= 100 DO␍␊\tIF rem(i, 3) == 0 AND rem(i, 5) == 0 DO␍␊\t\tprint(\"FizzBuzz\");␍␊\tELSE IF rem(i, 3) == 0 DO␍␊\t\tprint(\"Fizz\");␍␊\tELSE IF rem(i, 5) == 0 DO␍␊\t\tprint(\"Buzz\");␍␊\tELSE␍␊\t\tprint(i);␍␊\tEND END END␍␊\ti = i + 1;␍␊END", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.INTEGER, "123", 4)
                ))
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        );
    }

    @Test
    void testException() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"unterminated").lex());
        Assertions.assertEquals(13, exception.getIndex());
    }

    /**
     * Tests that lexing the input through {@link Lexer#lexToken()} produces a
     * single token with the expected type and literal matching the input.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            } else {
                Assertions.assertNotEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

    /**
     * Tests that lexing the input through {@link Lexer#lex()} matches the
     * expected token list.
     */
    private static void test(String input, List<Token> expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(expected, new Lexer(input).lex());
            } else {
                Assertions.assertNotEquals(expected, new Lexer(input).lex());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}
