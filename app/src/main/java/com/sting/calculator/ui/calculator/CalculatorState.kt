package com.sting.calculator.ui.calculator

data class CalculatorState(
    val expression: String = "",
    val display: String = "0",
    val isError: Boolean = false,
    val pendingOperator: String? = null,
    val firstOperand: Double? = null,
    val isNewInput: Boolean = true,
    val hasDecimal: Boolean = false
)
