package com.pbergman.ags.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data classes for configuration files with proper JSON property mappings
 */
data class AgentConfig(
    val id: String,
    val name: String,
    val role: String,
    @JsonProperty("system-prompt") val systemPrompt: String
)

data class ChannelConfig(
    val id: String,
    val name: String,
    val description: String,
    val public: Boolean
)

data class SubscriptionConfig(
    @JsonProperty("agent-id") val agentId: String,
    @JsonProperty("channel-id") val channelId: String
)

data class MessageConfig(
    @JsonProperty("channel-id") val channelId: String,
    @JsonProperty("agent-id") val agentId: String,
    val content: String
)

data class PromptTemplate(
    @JsonProperty("agent-response") val agentResponse: String,
    @JsonProperty("channel-welcome") val channelWelcome: String
)

data class AgentsConfig(val agents: List<AgentConfig>)
data class ChannelsConfig(val channels: List<ChannelConfig>)
data class SubscriptionsConfig(val subscriptions: List<SubscriptionConfig>)
data class InitialMessagesConfig(@JsonProperty("initial-messages") val initialMessages: List<MessageConfig>)
data class PromptTemplatesConfig(val templates: PromptTemplate)
