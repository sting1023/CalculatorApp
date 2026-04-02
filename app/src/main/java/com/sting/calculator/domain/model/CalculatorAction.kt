package com.sting.calculator.domain.model

sealed class CalculatorAction {
    data class Digit(val digit: String) : CalculatorAction()
    data class Operator(val operator: String) : CalculatorAction()
    object Decimal : CalculatorAction()
    object Equals : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object Percent : CalculatorAction()
}
