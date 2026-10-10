package com.bananahacks.agent.core

/**
 * Represents a tool that the AI agent can execute to gather information or perform actions.
 */
interface Tool {
    val name: String
    val description: String
    suspend fun execute(args: Map<String, String>): String
}

class TimeTool : Tool {
    override val name: String = "get_current_time"
    override val description: String = "Returns the current system time and date."
    override suspend fun execute(args: Map<String, String>): String {
        return "Current time: ${java.time.LocalDateTime.now()}"
    }
}

class CalculatorTool : Tool {
    override val name: String = "calculator"
    override val description: String = "Performs basic arithmetic calculations. Pass 'expr' in args."
    override suspend fun execute(args: Map<String, String>): String {
        val expr = args["expr"] ?: return "Error: No expression provided."
        return try {
            val result = evalExpression(expr)
            "Result of $expr = $result"
        } catch (e: Exception) {
            "Error evaluating expression: ${e.message}"
        }
    }

    private fun evalExpression(expr: String): Double {
        val sanitized = expr.filter { it.isDigit() || it in "+-*/()." }
        class Parser(val str: String) {
            var pos = -1
            var ch = ' '

            fun nextChar() {
                pos++
                ch = if (pos < str.length) str[pos] else '\u0000'
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected character: $ch")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+') -> x += parseTerm()
                        eat('-') -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*') -> x *= parseFactor()
                        eat('/') -> {
                            val divisor = parseFactor()
                            if (divisor == 0.0) throw RuntimeException("Division by zero")
                            x /= divisor
                        }
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor()
                if (eat('-')) return -parseFactor()

                val startPos = pos
                if (eat('(')) {
                    val x = parseExpression()
                    eat(')')
                    return x
                } else if ((ch in '0'..'9') || ch == '.') {
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    val numStr = str.substring(startPos, pos)
                    return numStr.toDoubleOrNull() ?: throw RuntimeException("Invalid number: $numStr")
                } else {
                    throw RuntimeException("Unexpected character: $ch")
                }
            }
        }
        return Parser(sanitized).parse()
    }
}

class WeatherTool : Tool {
    override val name: String = "get_weather"
    override val description: String = "Fetches mock weather for a given city."
    override suspend fun execute(args: Map<String, String>): String {
        val city = args["city"] ?: "Banana City"
        val temps = listOf(72, 68, 75, 80, 65)
        val conditions = listOf("Sunny", "Partly Cloudy", "Banana Breeze", "Clear")
        return "Weather in $city: ${temps.random()}°F, ${conditions.random()}"
    }
}
