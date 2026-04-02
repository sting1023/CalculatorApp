package com.sting.calculator.ui.calculator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sting.calculator.domain.model.CalculatorAction
import com.sting.calculator.ui.theme.Blue
import com.sting.calculator.ui.theme.BlueDark
import com.sting.calculator.ui.theme.DarkButtonText
import com.sting.calculator.ui.theme.DarkFunctionButtonBackground
import com.sting.calculator.ui.theme.DarkNumberButtonBackground
import com.sting.calculator.ui.theme.DarkOperatorButtonBackground
import com.sting.calculator.ui.theme.LightButtonText
import com.sting.calculator.ui.theme.LightFunctionButtonBackground
import com.sting.calculator.ui.theme.LightNumberButtonBackground
import com.sting.calculator.ui.theme.LightOperatorButtonBackground
import com.sting.calculator.ui.theme.Orange

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display area (30%)
            DisplayArea(
                expression = state.expression,
                display = state.display,
                isError = state.isError,
                modifier = Modifier.weight(0.3f)
            )

            // Button area (70%)
            CalculatorButtons(
                onAction = viewModel::onAction,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

@Composable
private fun DisplayArea(
    expression: String,
    display: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val displayColor = if (isError) Color.Red else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Expression line
        if (expression.isNotEmpty()) {
            Text(
                text = expression,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Main display
        val displayFontSize = when {
            display.length > 15 -> 32.sp
            display.length > 9 -> 40.sp
            else -> 56.sp
        }

        Text(
            text = display,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = displayFontSize,
                fontWeight = FontWeight.Bold
            ),
            color = displayColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CalculatorButtons(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: AC, ±, %, ÷
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FunctionButton(text = "AC", onClick = { onAction(CalculatorAction.Clear) }, modifier = Modifier.weight(1f))
            FunctionButton(text = "±", onClick = { onAction(CalculatorAction.ToggleSign) }, modifier = Modifier.weight(1f))
            FunctionButton(text = "%", onClick = { onAction(CalculatorAction.Percent) }, modifier = Modifier.weight(1f))
            OperatorButton(text = "÷", onClick = { onAction(CalculatorAction.Operator("÷")) }, modifier = Modifier.weight(1f))
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton(text = "7", onClick = { onAction(CalculatorAction.Digit("7")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "8", onClick = { onAction(CalculatorAction.Digit("8")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "9", onClick = { onAction(CalculatorAction.Digit("9")) }, modifier = Modifier.weight(1f))
            OperatorButton(text = "×", onClick = { onAction(CalculatorAction.Operator("×")) }, modifier = Modifier.weight(1f))
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton(text = "4", onClick = { onAction(CalculatorAction.Digit("4")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "5", onClick = { onAction(CalculatorAction.Digit("5")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "6", onClick = { onAction(CalculatorAction.Digit("6")) }, modifier = Modifier.weight(1f))
            OperatorButton(text = "-", onClick = { onAction(CalculatorAction.Operator("-")) }, modifier = Modifier.weight(1f))
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton(text = "1", onClick = { onAction(CalculatorAction.Digit("1")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "2", onClick = { onAction(CalculatorAction.Digit("2")) }, modifier = Modifier.weight(1f))
            NumberButton(text = "3", onClick = { onAction(CalculatorAction.Digit("3")) }, modifier = Modifier.weight(1f))
            OperatorButton(text = "+", onClick = { onAction(CalculatorAction.Operator("+")) }, modifier = Modifier.weight(1f))
        }

        // Row 5: 0 (span 2), ., =
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton(text = "0", onClick = { onAction(CalculatorAction.Digit("0")) }, modifier = Modifier.weight(2f))
            NumberButton(text = ".", onClick = { onAction(CalculatorAction.Decimal) }, modifier = Modifier.weight(1f))
            EqualsButton(onClick = { onAction(CalculatorAction.Equals) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isDark) DarkNumberButtonBackground else LightNumberButtonBackground
    val textColor = if (isDark) DarkButtonText else LightButtonText

    CalculatorButton(
        text = text,
        backgroundColor = backgroundColor,
        textColor = textColor,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun FunctionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isDark) DarkFunctionButtonBackground else LightFunctionButtonBackground
    val textColor = if (isDark) Color.White else LightButtonText

    CalculatorButton(
        text = text,
        backgroundColor = backgroundColor,
        textColor = textColor,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun OperatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isDark) DarkOperatorButtonBackground else Orange
    val textColor = Color.White

    CalculatorButton(
        text = text,
        backgroundColor = backgroundColor,
        textColor = textColor,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun EqualsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Blue
    val textColor = Color.White

    CalculatorButton(
        text = "=",
        backgroundColor = backgroundColor,
        textColor = textColor,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun CalculatorButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "button_scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) {
            backgroundColor.copy(alpha = 0.8f)
        } else {
            backgroundColor
        },
        label = "button_color"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(animatedColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
