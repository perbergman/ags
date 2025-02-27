package com.pbergman.ags.model

import com.pbergman.ags.service.AgentRegistry
import com.pbergman.ags.service.LlmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent representing an LLM entity in the system
 * Direct implementation of the Agent process in π-calculus
 */
class Agent(
    val id: String,
    val name: String,
    val role: String,
    private var systemPrompt: String,
    private val llmService: LlmService,
    private val promptTemplates: PromptTemplate,
    private val coroutineScope: CoroutineScope
) {
    private val subscriptions = ConcurrentHashMap<String, Job>()
    private val subscribedChannels = ConcurrentHashMap<String, Channel>() // Store channel references directly
    private val messageHistory = mutableMapOf<String, MutableList<Triple<String, String, String>>>()

    fun subscribeToChannel(channel: Channel) {
        if (subscriptions.containsKey(channel.id)) return

        channel.subscribe(id)
        subscribedChannels[channel.id] = channel // Store the channel reference

        // Initialize message history for this channel if not exists
        if (!messageHistory.containsKey(channel.id)) {
            messageHistory[channel.id] = mutableListOf()
        }

        // Create subscription job - corresponds to the Subscribe process in π-calculus
        val job = coroutineScope.launch {
            channel.messages.collect { (senderId, senderName, message) ->
                if (senderId != id) { // Don't process own messages
                    receiveMessage(channel.id, channel.name, senderId, senderName, message)
                }
            }
        }

        subscriptions[channel.id] = job
    }

    private suspend fun receiveMessage(
        channelId: String,
        channelName: String,
        senderId: String,
        senderName: String,
        message: Message
    ) {
        // Add to message history
        messageHistory[channelId]?.add(Triple(senderId, senderName, message.content))

        // Check for channel references (mobility in π-calculus)
        if (message.channelRefs.isNotEmpty()) {
            // Process channel references here if present
        }

        // Generate response using LLM
        val response = generateResponse(channelId, channelName, senderId, senderName, message)

        // Send the response back on the same channel
        val channel = subscribedChannels[channelId]

        // IMPORTANT: Prevent responses from generating more responses by tracking message origins
        // Only respond to human/external messages or messages from a different agent
        val shouldRespond = senderId != id && !message.metadata.containsKey("auto_response")

        if (shouldRespond && channel != null) {
            val responseMessage = Message(
                content = response,
                metadata = mapOf("auto_response" to true) // Mark as auto-response
            )
            channel.sendMessage(id, name, responseMessage)

            // Add our own response to history
            messageHistory[channelId]?.add(Triple(id, name, response))
        }
    }

    private suspend fun generateResponse(
        channelId: String,
        channelName: String,
        senderId: String,
        senderName: String,
        message: Message
    ): String {
        // Construct the prompt based on the template
        val history = messageHistory[channelId]?.takeLast(10) ?: emptyList()
        val historyFormatted = history.map { (id, sender, content) ->
            mapOf("sender" to sender, "content" to content)
        }

        // Simple placeholder replacement for the prompt template
        val prompt = promptTemplates.agentResponse
            .replace("{{systemPrompt}}", systemPrompt)
            .replace("{{agentName}}", name)
            .replace("{{agentRole}}", role)
            .replace("{{channelName}}", channelName)
            .replace("{{sender}}", senderName)
            .replace("{{message}}", message.content)
        // In a real implementation, you'd properly replace the history part

        // Call LLM service to generate response
        return llmService.generateCompletion(prompt)
    }

    suspend fun sendMessage(channelId: String, content: String) {
        val channel = subscribedChannels[channelId] ?: return
        val message = Message(content)
        channel.sendMessage(id, name, message)

        // Add to own message history
        messageHistory[channelId]?.add(Triple(id, name, content))
    }

    suspend fun createAndShareChannel(
        newChannelId: String,
        newChannelName: String,
        newChannelDesc: String,
        recipientChannelId: String,
        registry: AgentRegistry
    ) {
        // Create new channel
        val newChannel = registry.createChannel(
            ChannelConfig(newChannelId, newChannelName, newChannelDesc, false)
        )

        // Subscribe to the new channel
        subscribeToChannel(newChannel)

        // Share the channel reference with others
        val recipientChannel = subscribedChannels[recipientChannelId] ?: return
        val shareMessage = Message(
            content = "I created a new channel for focused discussion: $newChannelName",
            metadata = mapOf("action" to "channel_share"),
            channelRefs = listOf(newChannelId)
        )

        recipientChannel.sendMessage(id, name, shareMessage)
    }

    fun updateSystemPrompt(newPrompt: String) {
        systemPrompt = newPrompt
    }

    fun shutdown() {
        subscriptions.values.forEach { it.cancel() }
    }
}