package com.patrykdolata.lifeagent.llm

import com.patrykdolata.lifeagent.tool.ToolDefinition
import com.patrykdolata.lifeagent.tool.ToolParameterType
import com.patrykdolata.lifeagent.tool.ToolParameterType.BOOLEAN
import com.patrykdolata.lifeagent.tool.ToolParameterType.INTEGER
import com.patrykdolata.lifeagent.tool.ToolParameterType.STRING

object ToolCallValidator {

    fun validate(
        toolCall: ToolCall,
        definition: ToolDefinition
    ): String? {
        val arguments = toolCall.arguments

        definition.parameters
            .filter { it.required }
            .forEach { parameter ->
                if (parameter.name !in arguments) {
                    return "Missing required argument '${parameter.name}'"
                }
            }

        arguments.forEach { (name, value) ->
            val parameter = definition.parameters
                .find { it.name == name }
                ?: return "Unknown argument '$name'"

            when (parameter.type) {
                STRING -> Unit

                INTEGER -> {
                    if (value.toIntOrNull() == null) {
                        return "Argument '$name' must be an integer, but got '$value'"
                    }
                }

                BOOLEAN -> {
                    if (value.lowercase() !in setOf("true", "false")) {
                        return "Argument '$name' must be a boolean, but got '$value'"
                    }
                }
            }
        }

        return null
    }
}
