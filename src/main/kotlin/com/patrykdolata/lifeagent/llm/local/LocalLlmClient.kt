package com.patrykdolata.lifeagent.llm.local

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse
import com.patrykdolata.lifeagent.llm.Message
import com.patrykdolata.lifeagent.llm.Role
import com.patrykdolata.lifeagent.llm.Role.ASSISTANT
import com.patrykdolata.lifeagent.llm.Role.TOOL
import com.patrykdolata.lifeagent.llm.Role.USER
import com.patrykdolata.lifeagent.tool.ToolDefinition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class LocalLlmClient(
    private val model: String = "qwen3:1.7b"
) : LlmClient {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 120_000
        }
    }

    override fun generate(
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): LlmResponse = runBlocking {
        val request = OllamaChatRequest(
            model = model,
            messages = messages.map { message ->
                OllamaMessage(
                    role = message.role.toOllamaRole(),
                    content = message.content
                )
            },
            stream = false
        )

        val start = System.currentTimeMillis()
        val response = httpClient.post(
            "http://localhost:11434/api/chat"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<OllamaChatResponse>()
        val duration = System.currentTimeMillis() - start
        println("LLM request took ${duration}ms")

        LlmResponse.Text(
            content = response.message.content
        )
    }

    private fun Role.toOllamaRole(): String =
        when (this) {
            USER -> "user"
            ASSISTANT -> "assistant"
            TOOL -> "tool"
        }
}
