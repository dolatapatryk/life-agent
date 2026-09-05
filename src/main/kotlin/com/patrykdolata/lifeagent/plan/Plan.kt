package com.patrykdolata.lifeagent.plan

import kotlinx.serialization.Serializable

@Serializable
data class Plan(val steps: List<PlanStep>)

@Serializable
data class PlanStep(val id: String, val description: String)
