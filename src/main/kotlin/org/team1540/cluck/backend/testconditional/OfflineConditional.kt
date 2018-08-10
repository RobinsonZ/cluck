package org.team1540.cluck.backend.testconditional

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class OfflineConditional : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata) = "offline" !in context.environment.activeProfiles
}
