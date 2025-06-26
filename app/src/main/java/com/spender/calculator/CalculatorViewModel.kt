package com.spender.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

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
                if(_inputText.value.isNullOrEmpty() || _inputText.value == "0") {
                    _inputText.value = "0"
                }
                else {
                    _inputText.value = _inputText.value?.plus("00")
                }
            }
            else -> {
                _inputText.value = _inputText.value?.plus(button)
                _isResultFinal.value = false
            }
        }

        if(_inputText.value?.isNotEmpty() == true) {
            _resultText.value = _inputText.value?.let { evaluate(it) }
        }
    }


    private fun evaluate(value: String): String {
       val context : Context = Context.enter()
        context.optimizationLevel = -1
        val scriptable : Scriptable = context.initStandardObjects()
        return try {
            var result = context.evaluateString(scriptable, value.replace("×", "*").replace("÷", "/"), "Javascript", 1, null).toString().take(15)
            if (result.endsWith(".0")) {
                result = result.replace(".0", "")
            }
            result
        } catch (e: Exception) {
            "Expression Error"
        }
    }
}


