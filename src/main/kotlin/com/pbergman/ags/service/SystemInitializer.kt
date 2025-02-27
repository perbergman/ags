package com.pbergman.ags.service

import com.pbergman.ags.configuration.ConfigurationLoader
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * System initializer that sets up the entire agent system based on configuration
 * Implements the ConfigDrivenSystem process from π-calculus
 */
@Component
class SystemInitializer(
    private val configLoader: ConfigurationLoader,
    private val registry: AgentRegistry
) : CommandLineRunner {

    override fun run(vararg args: String) = runBlocking {
        // Load all configuration
        val agentsConfig = configLoader.loadAgents()
        val channelsConfig = configLoader.loadChannels()
        val subscriptionsConfig = configLoader.loadSubscriptions()
        val messagesConfig = configLoader.loadInitialMessages()
        val templatesConfig = configLoader.loadPromptTemplates()

        // Set templates in registry
        registry.setPromptTemplates(templatesConfig.templates)

        // Create all channels
        channelsConfig.channels.forEach { channelConfig ->
            registry.createChannel(channelConfig)
        }

        // Create all agents
        agentsConfig.agents.forEach { agentConfig ->
            registry.createAgent(agentConfig)
        }

        // Setup subscriptions
        subscriptionsConfig.subscriptions.forEach { subscription ->
            val agent = registry.getAgent(subscription.agentId)
            val channel = registry.getChannel(subscription.channelId)

            if (agent != null && channel != null) {
                agent.subscribeToChannel(channel)
            }
        }

        // Send initial messages
        messagesConfig.initialMessages.forEach { messageConfig ->
            val agent = registry.getAgent(messageConfig.agentId)

            if (agent != null) {
                // Updated: we no longer pass the registry parameter
                agent.sendMessage(messageConfig.channelId, messageConfig.content)
            }
        }

        println("System initialized with ${agentsConfig.agents.size} agents and ${channelsConfig.channels.size} channels")
    }
}