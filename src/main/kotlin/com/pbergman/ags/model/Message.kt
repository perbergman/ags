package com.pbergman.ags.model

/**
 * Represents a message in the agent communication system
 * Maps to messages in π-calculus model
 */
data class Message(
    val content: String,
    val metadata: Map<String, Any> = emptyMap(),
    val channelRefs: List<String> = emptyList()
)