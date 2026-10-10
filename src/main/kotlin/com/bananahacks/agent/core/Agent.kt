package com.bananahacks.agent.core

/**
 * Abstract base class for all AI agents in the system.
 */
abstract class Agent(
    val name: String,
    val description: String
) {
    protected val tools: MutableMap<String, Tool> = mutableMapOf()

    fun registerTool(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getToolNames(): List<String> = tools.keys.toList()

    abstract suspend fun processMessage(prompt: String, conversationId: String): String
}
