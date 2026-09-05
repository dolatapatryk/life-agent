package com.patrykdolata.lifeagent.plan

import com.patrykdolata.lifeagent.plan.PlanStepStatus.PENDING
import kotlinx.serialization.Serializable

@Serializable
data class Plan(val steps: List<PlanStep>)

@Serializable
data class PlanStep(
    val id: String,
    val description: String,
    val toolName: String? = null,
    val status: PlanStepStatus = PENDING
)

enum class PlanStepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class StepResult(
    val stepId: String,
    val toolName: String?,
    val result: String
)
