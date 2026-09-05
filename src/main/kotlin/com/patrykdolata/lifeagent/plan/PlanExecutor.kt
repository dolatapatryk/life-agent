package com.patrykdolata.lifeagent.plan

class PlanExecutor {

    fun execute(
        request: String,
        plan: Plan,
        executeStep: (
            originalRequest: String,
            step: PlanStep,
            previousResults: List<String>
        ) -> String
    ): String {
        val results = mutableListOf<String>()
        var finalResult: String? = null

        for (step in plan.steps) {
            val result = executeStep(
                request,
                step,
                results
            )

            results += "Krok ${step.id}: $result"
            finalResult = result
        }

        return finalResult ?: error("No steps in plan")
    }
}
