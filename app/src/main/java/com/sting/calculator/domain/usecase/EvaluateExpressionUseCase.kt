package com.sting.calculator.domain.usecase

import javax.inject.Inject
import kotlin.math.pow

class EvaluateExpressionUseCase @Inject constructor() {

    operator fun invoke(expression: String): Result<String> {
        return try {
            if (expression.isBlank()) {
                return Result.success("0")
            }

            val tokens = tokenize(expression)
            if (tokens.isEmpty()) {
                return Result.success("0")
            }

            val result = evaluate(tokens)
            Result.success(formatResult(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val expr = expression.replace("×", "*").replace("÷", "/").replace(" ", "")

        while (i < expr.length) {
            val char = expr[i]

            when {
                char.isDigit() || char == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        i++
                    }
                    tokens.add(Token.Number(expr.substring(start, i).toDouble()))
                }
                char in listOf('+', '-', '*', '/', '%') -> {
                    if (char == '-' && (tokens.isEmpty() || tokens.last() is Token.Operator)) {
                        // Negative number
                        i++
                        val start = i
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                            i++
                        }
                        if (start < i) {
                            tokens.add(Token.Number(-expr.substring(start, i).toDouble()))
                        }
                    } else {
                        val op = when (char) {
                            '+' -> Operator.ADD
                            '-' -> Operator.SUBTRACT
                            '*' -> Operator.MULTIPLY
                            '/' -> Operator.DIVIDE
                            '%' -> Operator.PERCENT
                            else -> throw IllegalArgumentException("Unknown operator: $char")
                        }
                        tokens.add(Token.Operator(op))
                    }
                }
                else -> i++
            }
        }

        return tokens
    }

    private fun evaluate(tokens: List<Token>): Double {
        if (tokens.isEmpty()) return 0.0

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Operator>()

        var i = 0
        while (i < tokens.size) {
            when (val token = tokens[i]) {
                is Token.Number -> {
                    numbers.add(token.value)
                }
                is Token.Operator -> {
                    operators.add(token.value)
                }
            }
            i++
        }

        // First pass: handle multiplication, division, and modulo
        var j = 0
        while (j < operators.size) {
            val op = operators[j]
            when (op) {
                Operator.MULTIPLY, Operator.DIVIDE, Operator.PERCENT -> {
                    val left = numbers[j]
                    val right = if (op == Operator.PERCENT) left * numbers[j + 1] / 100 else numbers[j + 1]

                    val result = when (op) {
                        Operator.MULTIPLY -> left * numbers[j + 1]
                        Operator.DIVIDE -> {
                            if (numbers[j + 1] == 0.0) throw ArithmeticException("Division by zero")
                            left / numbers[j + 1]
                        }
                        Operator.PERCENT -> left * numbers[j + 1] / 100
                        else -> left
                    }

                    numbers[j] = result
                    numbers.removeAt(j + 1)
                    operators.removeAt(j)
                }
                else -> j++
            }
        }

        // Second pass: handle addition and subtraction
        var result = numbers[0]
        j = 0
        for (op in operators) {
            when (op) {
                Operator.ADD -> result += numbers[j + 1]
                Operator.SUBTRACT -> result -= numbers[j + 1]
                else -> {}
            }
            j++
        }

        return result
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            val formatted = "%.10f".format(value).trimEnd('0').trimEnd('.')
            formatted
        }
    }

    sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val value: Operator) : Token()
    }

    enum class Operator {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT
    }
}
