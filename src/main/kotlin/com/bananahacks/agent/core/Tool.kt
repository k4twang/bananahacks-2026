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
            // Simple evaluation simulation for demo purposes
            val sanitized = expr.replace(Regex("[^0-9+\\-*/().]"), "")
            val result = javax.script.ScriptEngineManager().getEngineByName("JavaScript")?.eval(sanitized)
            "Result of $expr = $result"
        } catch (e: Exception) {
            "Error evaluating expression: ${e.message}"
        }
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
