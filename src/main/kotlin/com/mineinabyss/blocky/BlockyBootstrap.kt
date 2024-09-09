package com.mineinabyss.blocky

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key

class BlockyBootstrap : PluginBootstrap {
    private val MINEABLE_AXE = TagKey.create(RegistryKey.BLOCK, Key.key("mineable/axe"))
    override fun bootstrap(context: BootstrapContext) {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.BLOCK)) { event ->
            val registrar = event.registrar()
            val mineableAxeTag = registrar.getTag(MINEABLE_AXE)
            registrar.setTag(MINEABLE_AXE, mineableAxeTag.filter { "note_block" !in it.key().asString() })
        }
    }
}