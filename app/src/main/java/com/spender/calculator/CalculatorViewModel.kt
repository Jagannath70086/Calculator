package com.spender.calculator

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@Keep
class CalculatorViewModel : ViewModel() {

    private val _inputText = MutableLiveData("")
    val inputText: MutableLiveData<String> = _inputText

    private val _resultText = MutableLiveData("")
    val resultText: MutableLiveData<String> = _resultText

    private val _isResultFinal = MutableLiveData(false)
    val isResultFinal: MutableLiveData<Boolean> = _isResultFinal

    fun onButtonClick(button: String){
        when(button){
            "AC" -> {
                _inputText.value = ""
                _resultText.value = ""
                _isResultFinal.value = false
            }
            "DEL" -> {
                _inputText.value = _inputText.value?.dropLast(1)
                if(_inputText.value.isNullOrEmpty()) {
                    _resultText.value = ""
                }
                _isResultFinal.value = false
            }
            "=" -> {
                _isResultFinal.value = true
            }
            "00" -> {
                val currentInput = _inputText.value ?: ""
                _inputText.value = handleDoubleZeroInput(currentInput)
            }
            "0" -> {
                val currentInput = _inputText.value ?: ""
                _inputText.value = handleZeroInput(currentInput)
                _isResultFinal.value = false
            }
            "+", "-", "×", "÷", "%" -> {
                val currentInput = _inputText.value ?: ""
                _inputText.value = handleOperatorInput(currentInput, button)
                _isResultFinal.value = false
            }
            "." -> {
                val currentInput = _inputText.value ?: ""
                _inputText.value = handleDecimalInput(currentInput)
                _isResultFinal.value = false
            }
            else -> {
                val currentInput = _inputText.value ?: ""
                if(currentInput == "0") {
                    _inputText.value = button
                } else {
                    _inputText.value = currentInput + button
                }
                _isResultFinal.value = false
            }
        }

        if(_inputText.value?.isNotEmpty() == true) {
            _resultText.value = _inputText.value?.let { evaluate(it) }
        }
    }

    private fun Char.isOperator(): Boolean {
        return this in listOf('+', '-', '×', '÷', '%')
    }

    private fun String.trimEndWhile(predicate: (Char) -> Boolean): String {
        var endIndex = this.length
        while (endIndex > 0 && predicate(this[endIndex - 1])) {
            endIndex--
        }
        return this.substring(0, endIndex)
    }


    private fun handleOperatorInput(currentInput: String, newOperator: String): String {
        if (currentInput.isEmpty()) {
            return if (newOperator == "-") "-" else ""
        }

        val lastChar = currentInput.last()

        return when {
            lastChar.isOperator() -> {
                when {
                    lastChar in listOf('×', '÷', '%') && newOperator == "-" -> {
                        currentInput + newOperator
                    }
                    else -> currentInput.dropLast(1) + newOperator
                }
            }
            lastChar == ')' || lastChar.isDigit() -> {
                currentInput + newOperator
            }
            lastChar == '.' -> currentInput
            else -> currentInput + newOperator
        }
    }

    private fun handleDecimalInput(currentInput: String): String {
        if (currentInput.isEmpty()) {
            return "0."
        }

        val lastNumberStart = findLastNumberStart(currentInput)
        val lastNumber = currentInput.substring(lastNumberStart)

        return if (lastNumber.contains('.')) {
            currentInput
        } else if (currentInput.last().isOperator() || currentInput.last() == '(') {
            currentInput + "0."
        } else {
            "$currentInput."
        }
    }

    private fun handleZeroInput(currentInput: String): String {
        if (currentInput.isEmpty()) {
            return "0"
        }

        val lastChar = currentInput.last()

        when {
            lastChar.isOperator() || lastChar == '(' -> {
                return currentInput + "0"
            }

            currentInput == "0" -> {
                return "0"
            }

            else -> {
                val lastNumberStart = findLastNumberStart(currentInput)
                val lastNumber = currentInput.substring(lastNumberStart)

                return when {
                    // If the last number is just "0" without decimal, don't add another zero
                    lastNumber == "0" -> currentInput

                    // If the last number contains a decimal, allow zeros after decimal (like 2.50)
                    lastNumber.contains('.') -> currentInput + "0"

                    // If the last number doesn't start with "0", allow zero (like 25 → 250)
                    !lastNumber.startsWith("0") -> currentInput + "0"

                    // allow the zero
                    else -> currentInput + "0"
                }
            }
        }
    }

    private fun handleDoubleZeroInput(currentInput: String): String {
        if (currentInput.isEmpty()) {
            return "0"
        }

        val lastChar = currentInput.last()

        when {
            lastChar.isOperator() || lastChar == '(' -> {
                return currentInput + "0"
            }

            currentInput == "0" -> {
                return "0"
            }

            else -> {
                val lastNumberStart = findLastNumberStart(currentInput)
                val lastNumber = currentInput.substring(lastNumberStart)

                return when {
                    // If the last number is just "0" (like in "2+0"), don't add anything
                    lastNumber == "0" -> currentInput

                    // If the last number contains a decimal, allow "00" (like 2.5 → 2.500)
                    lastNumber.contains('.') -> currentInput + "00"

                    // If the last number doesn't start with "0", allow "00" (like 25 → 2500)
                    !lastNumber.startsWith("0") -> currentInput + "00"

                    // don't add anything
                    else -> currentInput
                }
            }
        }
    }

    private fun findLastNumberStart(input: String): Int {
        var i = input.length - 1
        while (i >= 0 && (input[i].isDigit() || input[i] == '.')) {
            i--
        }
        return i + 1
    }


    private fun evaluate(expression: String): String {
        return try {
            val result = evaluateExpression(expression.replace(" ", ""))
            if (result % 1.0 == 0.0) {
                result.toInt().toString().take(15)
            } else {
                result.toString().take(15)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun addImplicitMultiplication(expr: String): String {
        if (expr.length < 2) return expr

        val result = StringBuilder()
        var i = 0

        while (i < expr.length) {
            val current = expr[i]
            result.append(current)

            if (i < expr.length - 1) {
                val next = expr[i + 1]

                // 1. Number followed by opening parenthesis: "5(" -> "5*("
                // 2. Closing parenthesis followed by number: ")5" -> ")*5"
                // 3. Closing parenthesis followed by opening parenthesis: ")(" -> ")*("
                val shouldAddMult = when {
                    // case 1
                    (current.isDigit() || current == '.') && next == '(' -> true
                    // case 2
                    current == ')' && (next.isDigit() || next == '.') -> true
                    // case 3
                    current == ')' && next == '(' -> true
                    else -> false
                }

                if (shouldAddMult) {
                    result.append('*')
                }
            }

            i++
        }

        return result.toString()
    }

    private fun addMultipleOperator(expr: String): String {
        if (expr.length < 2) return expr

        val result = StringBuilder()
        var i = 0

        while (i < expr.length) {
            val current = expr[i]

            if (i < expr.length - 1 && current in listOf('*', '×', '/', '÷', '^') && expr[i + 1] == '-') {
                result.append(current)
                result.append("(-")
                i += 2

                // Collect the number following '-'
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    result.append(expr[i])
                    i++
                }

                result.append(")")
            } else {
                result.append(current)
                i++
            }
        }

        return result.toString()
    }


    private fun evaluateExpression(expr: String): Double {
        if (expr.isEmpty()) return 0.0

        val normalizedExpr = expr
            .replace("×", "*")
            .replace("÷", "/")

        val withImplicitMultiplication = addImplicitMultiplication(normalizedExpr)

        val cleanExpr = withImplicitMultiplication.trimEndWhile {
            it in listOf('+', '-', '*', '/', '%', '.')
        }
        if (cleanExpr.isEmpty()) return 0.0

        val withMultipleOperator = addMultipleOperator(cleanExpr)

        return parseExpression(withMultipleOperator)
    }

    private fun parseExpression(expr: String): Double {
        // addition and subtraction (lowest precedence)
        var i = expr.length - 1
        var level = 0

        while (i >= 0) {
            when (val char = expr[i]) {
                ')' -> level++
                '(' -> level--
                '+', '-' -> {
                    if (level == 0 && i > 0) {
                        val left = parseExpression(expr.substring(0, i))
                        val right = parseExpression(expr.substring(i + 1))
                        return if (char == '+') left + right else left - right
                    }
                }
            }
            i--
        }

        // multiplication, division, and modulo (higher precedence)
        i = expr.length - 1
        level = 0

        while (i >= 0) {
            when (val char = expr[i]) {
                ')' -> level++
                '(' -> level--
                '*', '/', '%' -> {
                    if (level == 0) {
                        val left = parseExpression(expr.substring(0, i))
                        val right = parseExpression(expr.substring(i + 1))
                        return when (char) {
                            '*' -> left * right
                            '/' -> {
                                if (right == 0.0) throw ArithmeticException("Division by zero")
                                left / right
                            }
                            '%' -> {
                                if (right == 0.0) throw ArithmeticException("Division by zero")
                                left % right
                            }
                            else -> 0.0
                        }
                    }
                }
            }
            i--
        }

        // Handle parentheses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            return parseExpression(expr.substring(1, expr.length - 1))
        }

        // Handle unary minus
        if (expr.startsWith("-")) {
            return -parseExpression(expr.substring(1))
        }

        // Handle unary plus
        if (expr.startsWith("+")) {
            return parseExpression(expr.substring(1))
        }

        // Parse number
        return expr.toDoubleOrNull() ?: throw NumberFormatException("Invalid number: $expr")
    }
}


