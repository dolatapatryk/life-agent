package com.patrykdolata.lifeagent.plan

import com.patrykdolata.lifeagent.tool.ToolDefinition

interface Planner {

    fun createPlan(request: String, tools: List<ToolDefinition>) : Plan
}
