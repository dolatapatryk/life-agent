package com.patrykdolata.lifeagent.plan

data class Plan(val steps: List<PlanStep>)

data class PlanStep(val description: String)
