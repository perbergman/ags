package com.pbergman.ags.service

import com.pbergman.ags.model.*
import kotlinx.coroutines.CoroutineScope
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for agents and channels
 * Implements the Registry process from π-calculus
 */
@Service
class AgentRegistry(
    private val llmService: LlmService,
    private val coroutineScope: CoroutineScope
) {
    private val agents = ConcurrentHashMap<String, Agent>()
    private val channels = ConcurrentHashMap<String, Channel>()
    private lateinit var promptTemplates: PromptTemplate

    fun setPromptTemplates(templates: PromptTemplate) {
        this.promptTemplates = templates
    }

    fun createAgent(config: AgentConfig): Agent {
        val agent = Agent(
            id = config.id,
            name = config.name,
            role = config.role,
            systemPrompt = config.systemPrompt,
            llmService = llmService,
            promptTemplates = promptTemplates,
            coroutineScope = coroutineScope
        )

        agents[config.id] = agent
        return agent
    }

    fun createChannel(config: ChannelConfig): Channel {
        val channel = Channel(
            id = config.id,
            name = config.name,
            description = config.description,
            isPublic = config.public
        )

        channels[config.id] = channel
        return channel
    }

    fun getAgent(id: String): Agent? = agents[id]
    fun getChannel(id: String): Channel? = channels[id]

    fun listAgents(): List<Map<String, String>> = agents.values.map {
        mapOf("id" to it.id, "name" to it.name, "role" to it.role)
    }

    fun listChannels(): List<Map<String, Any>> = channels.values.map {
        mapOf(
            "id" to it.id,
            "name" to it.name,
            "description" to it.description,
            "public" to it.isPublic,
            "subscriberCount" to it.getSubscriberCount()
        )
    }
}