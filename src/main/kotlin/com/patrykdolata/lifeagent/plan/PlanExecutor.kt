package com.patrykdolata.lifeagent.plan

import com.patrykdolata.lifeagent.plan.StepResultStatus.FAILURE
import com.patrykdolata.lifeagent.plan.StepResultStatus.SUCCESS
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
            val successfulStepIds = results
                .filter { it.status == SUCCESS }
                .map { it.stepId }
                .toSet()

            val failedStepIds = results
                .filter { it.status == FAILURE }
                .map { it.stepId }
                .toSet()
            val blockedSteps = pendingSteps.filter { step ->
                step.dependsOn.any { dependencyId ->
                    dependencyId in failedStepIds
                }
            }
            blockedSteps.forEach { step ->
                results += StepResult(
                    stepId = step.id,
                    toolName = step.toolName,
                    status = FAILURE,
                    result = "Krok nie został wykonany, ponieważ jeden z jego kroków zależnych zakończył się błędem"
                )
            }
            pendingSteps.removeAll(blockedSteps)

            val readySteps = pendingSteps.filter { step ->
                step.dependsOn.all { it in successfulStepIds }
            }

            if (readySteps.isEmpty() && blockedSteps.isEmpty()) {
                error("Cannot execute plan: unresolved dependencies")
            }

            val parallelSteps = readySteps.filter(canExecuteInParallel)
            val sequentialSteps = readySteps.filterNot(canExecuteInParallel)

            val parallelResults = coroutineScope {
                parallelSteps.map { step ->
                    val dependencyResults = results.filter { result -> result.stepId in step.dependsOn }
                    async(Dispatchers.IO) {
                        println("start async step: $step")
                        val result = executeStep(request, step, dependencyResults)
                        println("finish async step: $step")
                        result
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
