package com.sting.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import com.sting.calculator.domain.model.CalculatorAction
import com.sting.calculator.domain.usecase.EvaluateExpressionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val evaluateExpressionUseCase: EvaluateExpressionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    // 跟踪上一次计算结果，用于"连续按="的场景
    private var lastResult: Double? = null
    private var lastOperator: String? = null

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Digit -> appendDigit(action.digit)
            is CalculatorAction.Operator -> handleOperator(action.operator)
            is CalculatorAction.Decimal -> appendDecimal()
            is CalculatorAction.Equals -> calculate()
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.ToggleSign -> toggleSign()
            is CalculatorAction.Percent -> applyPercent()
        }
    }

    private fun appendDigit(digit: String) {
        _state.update { currentState ->
            // 每次新输入数字，清除"上次计算结果"的记忆
            lastResult = null
            lastOperator = null

            val newDisplay = when {
                currentState.isError -> digit
                currentState.isNewInput -> digit
                currentState.display == "0" -> digit
                currentState.display == "0." -> "0." + digit  // 小数点后追加
                currentState.display.length >= 15 -> currentState.display
                else -> currentState.display + digit
            }
            currentState.copy(
                display = newDisplay,
                isNewInput = false,
                isError = false
            )
        }
    }

    private fun handleOperator(operator: String) {
        _state.update { currentState ->
            // 如果有上次按=的结果，继续用结果做运算
            if (lastResult != null && lastOperator == null) {
                currentState.copy(
                    expression = "${lastResult!!} $operator ",
                    firstOperand = lastResult,
                    pendingOperator = operator,
                    isNewInput = true
                )
            } else if (currentState.pendingOperator != null && !currentState.isNewInput) {
                // 用户已输入数字，按运算符应该先计算中间结果
                val result = evaluateExpressionUseCase("${currentState.expression}${currentState.display}").getOrNull()
                    ?: return@update currentState
                currentState.copy(
                    display = result,
                    expression = "$result $operator ",
                    firstOperand = result.toDoubleOrNull(),
                    pendingOperator = operator,
                    isNewInput = true,
                    hasDecimal = result.contains(".")
                )
            } else {
                // 第一个运算符
                currentState.copy(
                    expression = "${currentState.display} $operator ",
                    firstOperand = currentState.display.toDoubleOrNull(),
                    pendingOperator = operator,
                    isNewInput = true
                )
            }
        }
    }

    private fun appendDecimal() {
        _state.update { currentState ->
            if (currentState.isError) {
                currentState.copy(display = "0.", isNewInput = false, hasDecimal = true)
            } else if (currentState.isNewInput) {
                currentState.copy(display = "0.", isNewInput = false, hasDecimal = true)
            } else if (!currentState.hasDecimal) {
                currentState.copy(display = "${currentState.display}.", hasDecimal = true)
            } else {
                currentState
            }
        }
    }

    private fun calculate() {
        _state.update { currentState ->
            val expr: String
            val displayValue = currentState.display

            if (lastResult != null && lastOperator != null && currentState.isNewInput) {
                // 连续按=：用上次结果 op 当前显示数字
                expr = "${lastResult!!} ${lastOperator!!} $displayValue"
            } else if (currentState.pendingOperator == null) {
                // 没有待定运算符，按=应该是重复上次操作
                // 例如：按了 5 + = 应该等于 10
                if (lastResult != null && lastOperator != null) {
                    expr = "${lastResult!!} ${lastOperator!!} $displayValue"
                } else {
                    return@update currentState
                }
            } else {
                // 正常计算
                expr = "${currentState.expression}$displayValue"
            }

            val result = evaluateExpressionUseCase(expr).getOrNull()
                ?: return@update currentState.copy(display = "Error", isError = true)

            // 记住这次结果和运算符，供下次"连续按="使用
            val op = currentState.pendingOperator ?: lastOperator
            lastResult = result.toDoubleOrNull()
            lastOperator = if (currentState.pendingOperator != null) currentState.pendingOperator else lastOperator

            currentState.copy(
                display = result,
                expression = "$expr = $result",
                pendingOperator = null,
                firstOperand = null,
                isNewInput = true,
                hasDecimal = result.contains("."),
                isError = false
            )
        }
    }

    private fun clear() {
        lastResult = null
        lastOperator = null
        _state.value = CalculatorState()
    }

    private fun delete() {
        _state.update { currentState ->
            if (currentState.isError || currentState.isNewInput || currentState.display == "0") {
                currentState
            } else {
                val newDisplay = if (currentState.display.length <= 1) {
                    "0"
                } else {
                    currentState.display.dropLast(1)
                }
                currentState.copy(
                    display = newDisplay,
                    hasDecimal = newDisplay.contains(".")
                )
            }
        }
    }

    private fun toggleSign() {
        _state.update { currentState ->
            if (currentState.display == "0" || currentState.isError) return@update currentState

            val currentValue = currentState.display.toDoubleOrNull() ?: return@update currentState
            val toggledValue = -currentValue

            val newDisplay = if (toggledValue == toggledValue.toLong().toDouble()) {
                toggledValue.toLong().toString()
            } else {
                toggledValue.toString()
            }

            currentState.copy(display = newDisplay, hasDecimal = newDisplay.contains("."))
        }
    }

    private fun applyPercent() {
        _state.update { currentState ->
            if (currentState.isError) return@update currentState

            val currentValue = currentState.display.toDoubleOrNull() ?: return@update currentState
            val percentValue = currentValue / 100

            val newDisplay = if (percentValue == percentValue.toLong().toDouble()) {
                percentValue.toLong().toString()
            } else {
                "%.10f".format(percentValue).trimEnd('0').trimEnd('.')
            }

            currentState.copy(
                display = newDisplay,
                isNewInput = true,
                hasDecimal = newDisplay.contains(".")
            )
        }
    }
}
