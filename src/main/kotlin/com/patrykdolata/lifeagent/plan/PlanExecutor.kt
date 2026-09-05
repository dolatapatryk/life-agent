package com.patrykdolata.lifeagent.plan

class PlanExecutor {

    fun execute(
        request: String,
        plan: Plan,
        executeStep: (
            originalRequest: String,
            step: PlanStep,
            previousResults: List<StepResult>
        ) -> StepResult
    ): StepResult {
        val results = mutableListOf<StepResult>()

        for (step in plan.steps) {
            val result = executeStep(
                request,
                step,
                results
            )

            results += result
        }

        return results.lastOrNull() ?: error("No steps in plan")
    }
}
