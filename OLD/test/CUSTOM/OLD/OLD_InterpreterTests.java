package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

final class InterpreterTests {

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null));
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Nil", new Ast.Expr.Literal(null), Environment.NIL.getValue()), //remember, special case
                Arguments.of("Boolean", new Ast.Expr.Literal(true), true),
                Arguments.of("Integer", new Ast.Expr.Literal(BigInteger.ONE), BigInteger.ONE),
                Arguments.of("Decimal", new Ast.Expr.Literal(BigDecimal.ONE), BigDecimal.ONE),
                Arguments.of("Character", new Ast.Expr.Literal('c'), 'c'),
                Arguments.of("String", new Ast.Expr.Literal("string"), "string")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null));
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Literal", new Ast.Expr.Literal(BigInteger.ONE), BigInteger.ONE),
                Arguments.of("Binary",
                        new Ast.Expr.Binary("+",
                                new Ast.Expr.Literal(BigInteger.ONE),
                                new Ast.Expr.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(11)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null));
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("And",
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Literal(true),
                                new Ast.Expr.Literal(false)
                        ),
                        false
                ),
                Arguments.of("Or (Short Circuit)",
                        new Ast.Expr.Binary("OR",
                                new Ast.Expr.Literal(true),
                                new Ast.Expr.Access(Optional.empty(), "undefined")
                        ),
                        true
                ),
                Arguments.of("Less Than",
                        new Ast.Expr.Binary("<",
                                new Ast.Expr.Literal(BigInteger.ONE),
                                new Ast.Expr.Literal(BigInteger.TEN)
                        ),
                        true
                ),
                Arguments.of("Greater Than or Equal",
                        new Ast.Expr.Binary(">=",
                                new Ast.Expr.Literal(BigInteger.ONE),
                                new Ast.Expr.Literal(BigInteger.TEN)
                        ),
                        false
                ),
                Arguments.of("Equal",
                        new Ast.Expr.Binary("==",
                                new Ast.Expr.Literal(BigInteger.ONE),
                                new Ast.Expr.Literal(BigInteger.TEN)
                        ),
                        false
                ),
                Arguments.of("Concatenation",
                        new Ast.Expr.Binary("+",
                                new Ast.Expr.Literal("a"),
                                new Ast.Expr.Literal("b")
                        ),
                        "ab"
                ),
                Arguments.of("Addition",
                        new Ast.Expr.Binary("+",
                                new Ast.Expr.Literal(BigInteger.ONE),
                                new Ast.Expr.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(11)
                ),
                Arguments.of("Division",
                        new Ast.Expr.Binary("/",
                                new Ast.Expr.Literal(new BigDecimal("1.2")),
                                new Ast.Expr.Literal(new BigDecimal("3.4"))
                        ),
                        new BigDecimal("0.4")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, Ast ast, Object expected) {
        Scope scope = new Scope(null);
        scope.defineVariable("variable", Environment.create("variable"));
        Scope object = new Scope(null);
        object.defineVariable("field", Environment.create("object.field"));
        scope.defineVariable("object", new Environment.PlcObject(object, "object"));
        test(ast, expected, scope);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        new Ast.Expr.Access(Optional.empty(), "variable"),
                        "variable"
                ),
                Arguments.of("Field",
                        new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), "object")), "field"),
                        "object.field"
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, Ast ast, Object expected) {
        Scope scope = new Scope(null);
        scope.defineFunction("function", 0, args -> Environment.create("function"));
        Scope object = new Scope(null);
        object.defineFunction("method", 1, args -> Environment.create("object.method"));
        scope.defineVariable("object", new Environment.PlcObject(object, "object"));
        test(ast, expected, scope);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Function",
                        new Ast.Expr.Function(Optional.empty(), "function", Arrays.asList()),
                        "function"
                ),
                Arguments.of("Method",
                        new Ast.Expr.Function(Optional.of(new Ast.Expr.Access(Optional.empty(), "object")), "method", Arrays.asList()),
                        "object.method"
                ),
                Arguments.of("Print",
                        new Ast.Expr.Function(Optional.empty(), "print", Arrays.asList(new Ast.Expr.Literal("Hello, World!"))),
                        Environment.NIL.getValue()
                )
        );
    }

    private static Scope test(Ast ast, Object expected, Scope scope) {
        Interpreter interpreter = new Interpreter(scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.visit(ast).getValue());
        } else {
            Assertions.assertThrows(RuntimeException.class, () -> interpreter.visit(ast));
        }
        return interpreter.getScope();
    }

}
