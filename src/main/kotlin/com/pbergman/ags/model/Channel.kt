package com.pbergman.ags.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Communication channel for agents
 * Direct implementation of channels in π-calculus
 */
class Channel(
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean
) {
    // Message is a triple of (senderId, senderName, message)
    private val _messages = MutableSharedFlow<Triple<String, String, Message>>(replay = 0)
    val messages = _messages.asSharedFlow()
    private val subscribers = ConcurrentHashMap.newKeySet<String>()

    suspend fun sendMessage(senderId: String, senderName: String, message: Message) {
        _messages.emit(Triple(senderId, senderName, message))
    }

    fun subscribe(agentId: String) {
        subscribers.add(agentId)
    }

    fun unsubscribe(agentId: String) {
        subscribers.remove(agentId)
    }

    fun getSubscriberCount(): Int = subscribers.size
    fun isSubscribed(agentId: String): Boolean = subscribers.contains(agentId)
    fun getSubscribers(): Set<String> = subscribers.toSet()
}