package com.sting.calculator.domain.usecase

import javax.inject.Inject

class EvaluateExpressionUseCase @Inject constructor() {

    operator fun invoke(expression: String): Result<String> {
        return try {
            if (expression.isBlank()) return Result.success("0")
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return Result.success("0")
            val result = evaluate(tokens)
            Result.success(formatResult(result))
        } catch (e: ArithmeticException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val expr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
            .replace("+", "+")

        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            when {
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    tokens.add(Token.Number(expr.substring(start, i).toDouble()))
                }
                ch == '-' -> {
                    val prev = tokens.lastOrNull()
                    if (prev is Token.Number) {
                        tokens.add(Token.Operator(Op.SUBTRACT)); i++
                    } else {
                        // 负号
                        i++
                        val start = i
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                        if (start < i) {
                            tokens.add(Token.Number(-expr.substring(start, i).toDouble()))
                        } else {
                            tokens.add(Token.Operator(Op.SUBTRACT))
                        }
                    }
                }
                ch == '+' -> { tokens.add(Token.Operator(Op.ADD)); i++ }
                ch == '*' -> { tokens.add(Token.Operator(Op.MULTIPLY)); i++ }
                ch == '/' -> { tokens.add(Token.Operator(Op.DIVIDE)); i++ }
                ch == '%' -> { tokens.add(Token.Operator(Op.PERCENT)); i++ }
                else -> i++
            }
        }
        return tokens
    }

    private fun evaluate(tokens: List<Token>): Double {
        if (tokens.isEmpty()) return 0.0

        // 第一遍：乘除（含百分号转小数）
        val stage1 = mutableListOf<Token>()
        var i = 0
        while (i < tokens.size) {
            val t = tokens[i]
            if (t is Token.Operator && (t.value == Op.MULTIPLY || t.value == Op.DIVIDE)) {
                if (stage1.isEmpty() || stage1.lastOrNull() !is Token.Number) {
                    stage1.add(t); i++; continue
                }
                val left = (stage1.removeLast() as Token.Number).value
                val rightToken = tokens.getOrNull(i + 1)
                if (rightToken !is Token.Number) {
                    stage1.add(t); i++; continue
                }
                val right = rightToken.value
                val result = when (t.value) {
                    Op.DIVIDE -> { if (right == 0.0) throw ArithmeticException("Division by zero"); left / right }
                    Op.MULTIPLY -> left * right
                    else -> left
                }
                stage1.add(Token.Number(result))
                i += 2
            } else {
                // 百分号 → 乘0.01
                if (t is Token.Operator && t.value == Op.PERCENT) {
                    if (stage1.isNotEmpty() && stage1.lastOrNull() is Token.Number) {
                        val prev = (stage1.removeLast() as Token.Number).value
                        stage1.add(Token.Number(prev * 0.01))
                    }
                } else {
                    stage1.add(t)
                }
                i++
            }
        }

        // 第二遍：加减，从左到右
        var result = 0.0
        var currentOp: Op? = null
        for (t in stage1) {
            when (t) {
                is Token.Number -> {
                    result = when (currentOp) {
                        null -> t.value
                        Op.ADD -> result + t.value
                        Op.SUBTRACT -> result - t.value
                        else -> t.value
                    }
                    currentOp = null
                }
                is Token.Operator -> {
                    if (t.value == Op.ADD || t.value == Op.SUBTRACT) {
                        currentOp = t.value
                    }
                }
            }
        }
        return result
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.10f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val value: Op) : Token()
    }

    enum class Op { ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT }
}
