package com.spender.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable

val buttonLayout4 = listOf(
    "AC", "%", "DEL", "÷",
    "7", "8", "9", "×",
    "4", "5", "6", "-",
    "1", "2", "3", "+",
    "00", "0", ".", "=",
)

val buttonLayout5 = listOf(
    "sin", "cos", "tan", "rad", "deg",
    "log", "ln", "(", ")", "inv",
    "!", "AC", "%", "DEL", "÷",
    "^", "7", "8", "9", "×",
    "root", "4", "5", "6", "-",
    "pi", "1", "2", "3", "+",
    "e", "00", "0", ".", "="
)

@Composable
fun Calculator(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel
) {
    val columns: MutableState<Int> = rememberSaveable { mutableIntStateOf(4) }
    val iconRes = if (columns.value == 4) R.drawable.advanced_calc else R.drawable.normal_calc

    val inputText = viewModel.inputText.observeAsState()
    val resultText = viewModel.resultText.observeAsState()
    val isResultFinal by viewModel.isResultFinal.observeAsState(false)

    val inputFontSize by animateIntAsState(targetValue = if (isResultFinal) 26 else 48, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = "")
    val inputColor by animateColorAsState(targetValue = if (isResultFinal) Color.Gray else Color.White, label = "")

    val resultFontSize by animateIntAsState(targetValue = if (isResultFinal) 48 else 26, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = "")
    val resultColor by animateColorAsState(targetValue = if (isResultFinal) Color.White else Color.Gray, label = "")

    Box(modifier = modifier.background(Color.Black)){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Row {
                IconButton(
                    onClick = {
                        columns.value = if (columns.value == 4) 5 else 4
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle Mode",
                        tint = Color.Unspecified
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = inputText.value ?: "",
                style = TextStyle(
                    fontSize = inputFontSize.sp,
                    color = inputColor,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .padding(end = 11.dp, bottom = 5.dp)
                    .fillMaxWidth()
            )

            Text(
                text = resultText.value ?: "",
                style = TextStyle(
                    fontSize = resultFontSize.sp,
                    textAlign = TextAlign.End,
                    color = resultColor
                ),
                modifier = Modifier
                    .padding(end = 11.dp, bottom = 20.dp)
                    .fillMaxWidth()
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns.value),
                modifier = Modifier.padding(bottom = 23.dp)
            ) {
                items(if(columns.value == 4) buttonLayout4 else buttonLayout5) {
                    CalculatorButton(
                        text = it,
                        onClick = { viewModel.onButtonClick(it) },
                        buttonSize = getButtonSize(columns.value)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    buttonSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(6.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            contentColor = Color.White,
            containerColor = getColor(text)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}


fun getColor(name: String) : Color {
    return when(name) {
        "DEL", "AC", "%", "÷", "×", "-", "+" -> Color(0xff1e1e1e)
        "=" -> Color(0xffed6825)
        else -> Color(0xff2e2e2e)
    }
}

@Composable
fun getButtonSize(columns : Int) : Dp {
    val configuration = LocalConfiguration.current

    val screenWidthDp = configuration.screenWidthDp
    val buttonPadding = 18.dp

    val totalHorizontalPadding = buttonPadding * columns

    val availableWidth = screenWidthDp.dp - totalHorizontalPadding

    return availableWidth/columns
}
