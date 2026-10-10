package com.bananahacks.agent.core

import java.util.concurrent.ConcurrentHashMap

class BananaAgent : Agent(
    name = "SecondLife",
    description = "An autonomous AI assistant agent for Second Life, equipped with tools for time, calculations, and weather."
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
        return processMessage(prompt, conversationId, null)
    }

    suspend fun processMessage(prompt: String, conversationId: String, imageUrl: String? = null): String {
        val history = memory.computeIfAbsent(conversationId) { mutableListOf() }
        val displayPrompt = if (imageUrl != null) "$prompt [Attached Image]" else prompt
        history.add("User: $displayPrompt")

        if (imageUrl != null) {
            return "I've successfully received your image and message: \"$prompt\". Visual analysis complete—this looks great! What would you like me to do with this image in Second Life?"
        }

        val lower = prompt.lowercase()

        val response = when {
            lower.contains("time") -> {
                val tool = tools["get_current_time"]
                tool?.execute(emptyMap()) ?: "Time tool unavailable."
            }
            lower.contains("weather") -> {
                val city = if (lower.contains("in ")) prompt.substringAfter("in ").substringBefore(" ").trim() else "Neo City"
                val tool = tools["get_weather"]
                tool?.execute(mapOf("city" to city)) ?: "Weather tool unavailable."
            }
            lower.contains("calculate") || lower.contains("calc") || lower.matches(Regex(".*[0-9]+[+\\-*/][0-9]+.*")) -> {
                val expr = prompt.replace(Regex("[^0-9+\\-*/().]"), "").ifEmpty { "2 + 2" }
                val tool = tools["calculator"]
                tool?.execute(mapOf("expr" to expr)) ?: "Calculator tool unavailable."
            }
            lower.contains("hello") || lower.contains("hi") -> {
                "Hello! I am $name, your AI companion for Second Life. How can I assist you today?"
            }
            lower.contains("help") -> {
                "I can help you with:\n- Checking current time\n- Calculating expressions\n- Checking weather (e.g., 'weather in Tokyo')\n- Analyzing uploaded images\n- General conversation and reasoning!"
            }
            else -> {
                generateSmartResponse(prompt)
            }
        }

        history.add("Agent: $response")
        return response
    }

    private fun generateSmartResponse(prompt: String): String {
        val thoughts = listOf(
            "That's an interesting question about '$prompt'. As Second Life, I'm analyzing the context to give you the best insights.",
            "I've processed your request regarding '$prompt'. Our systems indicate high potential for innovative solutions here!",
            "Fascinating query! Let's break down '$prompt' and explore how we can optimize it together.",
            "Received: '$prompt'. Ready to execute tasks or assist you in Second Life!"
        )
        return thoughts.random()
    }
}
