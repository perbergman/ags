package com.pbergman.ags.configuration

import com.pbergman.ags.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * Configuration loader component
 * Loads all configuration from YAML files
 */
@Component
class ConfigurationLoader(
    @Value("classpath:agents.yml") private val agentsResource: Resource,
    @Value("classpath:channels.yml") private val channelsResource: Resource,
    @Value("classpath:subscriptions.yml") private val subscriptionsResource: Resource,
    @Value("classpath:initial-messages.yml") private val messagesResource: Resource,
    @Value("classpath:prompt-templates.yml") private val templatesResource: Resource
) {
    private val objectMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }

    fun loadAgents(): AgentsConfig {
        return objectMapper.readValue(agentsResource.inputStream)
    }

    fun loadChannels(): ChannelsConfig {
        return objectMapper.readValue(channelsResource.inputStream)
    }

    fun loadSubscriptions(): SubscriptionsConfig {
        return objectMapper.readValue(subscriptionsResource.inputStream)
    }

    fun loadInitialMessages(): InitialMessagesConfig {
        return objectMapper.readValue(messagesResource.inputStream)
    }

    fun loadPromptTemplates(): PromptTemplatesConfig {
        return objectMapper.readValue(templatesResource.inputStream)
    }
}