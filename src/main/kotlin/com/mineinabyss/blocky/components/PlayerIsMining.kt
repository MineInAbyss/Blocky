package com.mineinabyss.blocky.components

import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable

@Serializable
class PlayerIsMining(
    var miningTask: Job? = null
)
