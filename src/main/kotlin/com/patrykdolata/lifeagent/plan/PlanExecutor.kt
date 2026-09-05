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
        validatePlan(plan)
        val pendingSteps = plan.steps.toMutableList()
        val results = mutableListOf<StepResult>()

        while (pendingSteps.isNotEmpty()) {
            val completedStepIds = results
                .map { it.stepId }
                .toSet()
            val readySteps = pendingSteps.filter { step ->
                step.dependsOn.all { it in completedStepIds }
            }

            check(readySteps.isNotEmpty()) {
                "Cannot execute plan: unresolved dependencies"
            }

            for (step in readySteps) {
                val dependencyResults = results.filter { result -> result.stepId in step.dependsOn }
                val result = executeStep(
                    request,
                    step,
                    dependencyResults
                )

                results += result
                pendingSteps.remove(step)
            }
        }

        return results.lastOrNull() ?: error("No steps in plan")
    }

    private fun validatePlan(plan: Plan) {
        val stepIds = plan.steps.map { it.id }.toSet()

        plan.steps.forEach { step ->
            step.dependsOn.forEach { dependencyId ->
                require(dependencyId in stepIds) {
                    "Step '${step.id}' depends on unknown step '$dependencyId'"
                }

                require(dependencyId != step.id) {
                    "Step '${step.id}' cannot depend on itself"
                }
            }
        }
    }
}
