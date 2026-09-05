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
            val readyStep = pendingSteps.firstOrNull { step ->
                step.dependsOn.all { it in completedStepIds }
            } ?: error("Cannot execute plan: unresolved dependencies")

            val dependencyResults = results.filter { it.stepId in readyStep.dependsOn }
            val result = executeStep(request, readyStep, dependencyResults)
            results += result
            pendingSteps.remove(readyStep)
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
