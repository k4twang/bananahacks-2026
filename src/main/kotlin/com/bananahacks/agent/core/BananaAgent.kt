package com.bananahacks.agent.core

import java.util.concurrent.ConcurrentHashMap

class BananaAgent : Agent(
    name = "BananaAgent-v1",
    description = "An autonomous assistant agent equipped with tools for time, calculations, and weather."
) {
    private val startTime = System.currentTimeMillis()
    private val memory = ConcurrentHashMap<String, MutableList<String>>()

    init {
        registerTool(TimeTool())
        registerTool(CalculatorTool())
        registerTool(WeatherTool())
    }

    fun getUptime(): Long = System.currentTimeMillis() - startTime

    override suspend fun processMessage(prompt: String, conversationId: String): String {
        val history = memory.computeIfAbsent(conversationId) { mutableListOf() }
        history.add("User: $prompt")

        val lower = prompt.lowercase()

        val response = when {
            lower.contains("time") -> {
                val tool = tools["get_current_time"]
                tool?.execute(emptyMap()) ?: "Time tool unavailable."
            }
            lower.contains("weather") -> {
                val city = if (lower.contains("in ")) prompt.substringAfter("in ").substringBefore(" ").trim() else "Banana City"
                val tool = tools["get_weather"]
                tool?.execute(mapOf("city" to city)) ?: "Weather tool unavailable."
            }
            lower.contains("calculate") || lower.contains("calc") || lower.matches(Regex(".*[0-9]+[+\\-*/][0-9]+.*")) -> {
                // Extract expression
                val expr = prompt.replace(Regex("[^0-9+\\-*/().]"), "").ifEmpty { "2 + 2" }
                val tool = tools["calculator"]
                tool?.execute(mapOf("expr" to expr)) ?: "Calculator tool unavailable."
            }
            lower.contains("hello") || lower.contains("hi") -> {
                "Hello! I am $name, your AI host agent for BananaHacks 2026. How can I assist you today?"
            }
            lower.contains("help") -> {
                "I can help you with:\n- Checking current time\n- Calculating expressions\n- Checking weather (e.g., 'weather in Tokyo')\n- General conversation and reasoning!"
            }
            else -> {
                // General creative conversational response
                generateSmartResponse(prompt)
            }
        }

        history.add("Agent: $response")
        return response
    }

    private fun generateSmartResponse(prompt: String): String {
        val thoughts = listOf(
            "That's an interesting question about '$prompt'. As BananaAgent, I'm analyzing the context to give you the best insights.",
            "I've processed your request regarding '$prompt'. Our systems indicate high potential for innovative solutions here!",
            "Fascinating query! Let's break down '$prompt' and explore how we can optimize it using AI agents.",
            "Received: '$prompt'. Ready to execute tasks or assist with your hackathon project!"
        )
        return thoughts.random()
    }
}
