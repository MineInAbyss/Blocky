package com.mineinabyss.blocky.components.mining

import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable

@Serializable
data class PlayerIsMining(var miningTask: Job? = null)
