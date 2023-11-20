package com.mineinabyss.blocky.components.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("blocky:migrations")
data class AppliedMigrations(
    val migrations: Map<String, Int> = mapOf()
)
