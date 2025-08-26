package com.mineinabyss.blocky

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key

class BlockyBootstrap : PluginBootstrap {
    private val MINEABLE_AXE = RegistryKey.BLOCK.tagKey("mineable/axe")
    private val NOTEBLOCK = RegistryKey.BLOCK.typedKey("note_block")

    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.BLOCK)) { event ->
            event.registrar().setTag(MINEABLE_AXE, event.registrar().getTag(MINEABLE_AXE).minus(NOTEBLOCK))
        }
    }
}