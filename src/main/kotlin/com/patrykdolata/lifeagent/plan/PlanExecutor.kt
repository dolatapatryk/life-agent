package com.patrykdolata.lifeagent.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class PlanExecutor {

    suspend fun execute(
        request: String,
        plan: Plan,
        canExecuteInParallel: (PlanStep) -> Boolean,
        executeStep: (
            originalRequest: String,
            step: PlanStep,
            previousResults: List<StepResult>
        ) -> StepResult
    ): PlanExecutionResult {
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

            val parallelSteps = readySteps.filter(canExecuteInParallel)
            val sequentialSteps = readySteps.filterNot(canExecuteInParallel)

            val parallelResults = coroutineScope {
                parallelSteps.map { step ->
                    val dependencyResults = results.filter { result -> result.stepId in step.dependsOn }
                    async(Dispatchers.IO) {
                        executeStep(request, step, dependencyResults)
                    }
                }.awaitAll()
            }
            results += parallelResults
            pendingSteps.removeAll(parallelSteps)

            for (step in sequentialSteps) {
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

        return PlanExecutionResult(results)
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
