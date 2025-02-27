package com.pbergman.ags.controller

import com.pbergman.ags.service.AgentRegistry
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AgentSystemController(private val registry: AgentRegistry) {

    @GetMapping("/agents")
    fun listAgents(): Map<String, List<Map<String, String>>> {
        return mapOf("agents" to registry.listAgents())
    }

    @GetMapping("/channels")
    fun listChannels(): Map<String, List<Map<String, Any>>> {
        return mapOf("channels" to registry.listChannels())
    }

    @PostMapping("/agents/{agentId}/send")
    fun sendMessage(
        @PathVariable agentId: String,
        @RequestBody request: SendMessageRequest
    ): Map<String, Any> {
        val agent = registry.getAgent(agentId)

        return if (agent != null) {
            // Launch the message sending asynchronously so it doesn't block the HTTP response
            kotlinx.coroutines.GlobalScope.launch {
                agent.sendMessage(request.channelId, request.content)
            }
            mapOf("success" to true)
        } else {
            mapOf("success" to false, "error" to "Agent not found")
        }
    }

    @PostMapping("/agents/{agentId}/create-channel")
    fun createChannel(
        @PathVariable agentId: String,
        @RequestBody request: CreateChannelRequest
    ): Map<String, Any> {
        val agent = registry.getAgent(agentId)

        return if (agent != null) {
            runBlocking {
                // This method still needs the registry
                agent.createAndShareChannel(
                    request.channelId,
                    request.name,
                    request.description,
                    request.shareOnChannelId,
                    registry
                )
            }
            mapOf("success" to true)
        } else {
            mapOf("success" to false, "error" to "Agent not found")
        }
    }
}

// Request/response data classes
data class SendMessageRequest(val channelId: String, val content: String)
data class CreateChannelRequest(
    val channelId: String,
    val name: String,
    val description: String,
    val shareOnChannelId: String
)