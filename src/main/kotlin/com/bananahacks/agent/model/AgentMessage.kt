package com.bananahacks.agent.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val prompt: String,
    val conversationId: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class ChatResponse(
    val response: String,
    val conversationId: String,
    val agentName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AgentStatusResponse(
    val agentName: String,
    val status: String,
    val activeTools: List<String>,
    val uptimeMs: Long
)
