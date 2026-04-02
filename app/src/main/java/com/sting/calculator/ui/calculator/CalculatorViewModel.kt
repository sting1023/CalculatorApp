package com.sting.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sting.calculator.domain.model.CalculatorAction
import com.sting.calculator.domain.usecase.EvaluateExpressionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val evaluateExpressionUseCase: EvaluateExpressionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onAction(action: CalculatorAction) {
        viewModelScope.launch {
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
    }

    private fun appendDigit(digit: String) {
        _state.update { currentState ->
            val newDisplay = if (currentState.isNewInput || currentState.display == "0") {
                digit
            } else {
                currentState.display + digit
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
            val currentValue = currentState.display.toDoubleOrNull() ?: return@update currentState

            val newExpression = when {
                currentState.pendingOperator != null && !currentState.isNewInput -> {
                    // User entered a number after an operator - calculate intermediate result
                    val result = evaluateExpressionUseCase("${currentState.expression}$currentValue")
                    val resultStr = result.getOrNull() ?: currentValue.toString()
                    "${resultStr} $operator "
                }
                currentState.pendingOperator == null -> {
                    // First operator pressed
                    "${currentState.display} $operator "
                }
                else -> {
                    // Replace pending operator
                    currentState.expression.dropLast(currentState.pendingOperator!!.length + 1) + "$operator "
                }
            }

            currentState.copy(
                firstOperand = currentValue,
                pendingOperator = operator,
                isNewInput = true,
                hasDecimal = false,
                expression = newExpression
            )
        }
    }

    private fun appendDecimal() {
        _state.update { currentState ->
            if (currentState.hasDecimal) return@update currentState

            val newDisplay = if (currentState.isNewInput) "0." else "${currentState.display}."
            currentState.copy(
                display = newDisplay,
                isNewInput = false,
                hasDecimal = true
            )
        }
    }

    private fun calculate() {
        _state.update { currentState ->
            if (currentState.pendingOperator == null) return@update currentState

            // Build complete expression: expression + display
            val fullExpression = currentState.expression + currentState.display

            evaluateExpressionUseCase(fullExpression).fold(
                onSuccess = { result ->
                    currentState.copy(
                        display = result,
                        expression = "$fullExpression = $result",
                        pendingOperator = null,
                        firstOperand = null,
                        isNewInput = true,
                        hasDecimal = result.contains("."),
                        isError = false
                    )
                },
                onFailure = {
                    currentState.copy(
                        display = "Error",
                        expression = "",
                        pendingOperator = null,
                        firstOperand = null,
                        isNewInput = true,
                        hasDecimal = false,
                        isError = true
                    )
                }
            )
        }
    }

    private fun clear() {
        _state.update { CalculatorState() }
    }

    private fun delete() {
        _state.update { currentState ->
            if (currentState.isError || currentState.isNewInput) return@update currentState

            val newDisplay = if (currentState.display.length <= 1) "0" else currentState.display.dropLast(1)
            val newHasDecimal = newDisplay.contains(".")

            currentState.copy(
                display = newDisplay,
                hasDecimal = newHasDecimal
            )
        }
    }

    private fun toggleSign() {
        _state.update { currentState ->
            if (currentState.display == "0") return@update currentState

            val currentValue = currentState.display.toDoubleOrNull() ?: return@update currentState
            val toggledValue = -currentValue

            val newDisplay = if (toggledValue == toggledValue.toLong().toDouble()) {
                toggledValue.toLong().toString()
            } else {
                toggledValue.toString()
            }

            currentState.copy(
                display = newDisplay,
                hasDecimal = newDisplay.contains(".")
            )
        }
    }

    private fun applyPercent() {
        _state.update { currentState ->
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
