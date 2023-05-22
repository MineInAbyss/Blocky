package com.mineinabyss.blocky

import com.mineinabyss.idofront.di.DI

val blocky by DI.observe<BlockyContext>()
interface BlockyContext {
    val plugin: BlockyPlugin
    val config: BlockyConfig
}
