package com.patrykdolata.lifeagent.tool

import java.time.LocalDateTime

class GetCurrentTimeTool : Tool {

    override val definition = ToolDefinition(
        name = "getCurrentTime",
        description = "Returns the current date and time.",
        parameters = emptyList()
    )

    override fun execute(arguments: Map<String, String>): String {
        return LocalDateTime.now().toString();
    }
}
