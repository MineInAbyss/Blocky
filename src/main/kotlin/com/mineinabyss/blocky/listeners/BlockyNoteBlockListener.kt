package com.mineinabyss.blocky.listeners

import com.mineinabyss.blocky.components.BlockType
import com.mineinabyss.blocky.components.BlockyBlock
import com.mineinabyss.blocky.components.BlockyInfo
import com.mineinabyss.blocky.helpers.getBlockyBlockDataFromItem
import com.mineinabyss.blocky.helpers.getBlockyDecorationDataFromItem
import com.mineinabyss.blocky.helpers.getPrefabFromBlock
import com.mineinabyss.blocky.helpers.handleBlockyDrops
import com.mineinabyss.blocky.systems.BlockBreakingSystem
import com.mineinabyss.blocky.systems.BlockHardnessModifiers
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.NotePlayEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class BlockyNoteBlockListener : Listener {

    @EventHandler
    fun NotePlayEvent.cancelBlockyNotes() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEvent.onChangingNote() {
        if (clickedBlock?.type == Material.NOTE_BLOCK && rightClicked) isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPhysicsEvent.onBlockPhysics() {
        val aboveBlock = block.location.add(0.0, 1.0, 0.0).block
        if (aboveBlock.type == Material.NOTE_BLOCK) {
            isCancelled = true
            updateAndCheck(block.location)
        }
        if (block.type == Material.NOTE_BLOCK) {
            isCancelled = true
            block.state.update(true, false)
        }
    }

    @EventHandler
    fun BlockPlaceEvent.onPlacingBlockyBlock() {
        itemInHand.toGearyOrNull(player)?.get<BlockyInfo>() ?: return
        val blockyBlock = itemInHand.toGearyOrNull(player)?.get<BlockyBlock>() ?: return

        if (blockyBlock.blockType == BlockType.CUBE) {
            block.setBlockData(block.getBlockyBlockDataFromItem(blockyBlock.blockId), false)
            return
        } else if (blockyBlock.blockType == BlockType.GROUND && blockAgainst.getFace(blockPlaced) == BlockFace.UP) {
            block.setType(Material.TRIPWIRE, false)
            block.setBlockData(block.getBlockyDecorationDataFromItem(blockyBlock.blockId, blockyBlock.blockType), true)
            blockAgainst.state.update(true, false)
            blockPlaced.state.update(true, false)
            return
        } else if (blockyBlock.blockType == BlockType.WALL && blockAgainst.getFace(blockPlaced) != BlockFace.UP) {
            block.setType(Material.GLOW_LICHEN, false)
            block.setBlockData(block.getBlockyDecorationDataFromItem(blockyBlock.blockId, blockyBlock.blockType), true)
        }
        block.setBlockData(block.blockData, false)
    }

    //TODO Try and somehow do custom break-times depending on item in hand etc
//    @EventHandler
//    fun BlockDamageEvent.onMiningBlockyBlock() {
//        val gearyBlock = block.getPrefabFromBlock() ?: return
//        val blocky = gearyBlock.get<BlockyInfo>() ?: return
//        if (blocky.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true
//    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakingBlockyBlock() {
        if (block.type != Material.NOTE_BLOCK || isCancelled || !isDropItems) return

        val prefab = block.getPrefabFromBlock() ?: return
        val blockyInfo = prefab.get<BlockyInfo>() ?: return

        if (blockyInfo.isUnbreakable && player.gameMode != GameMode.CREATIVE) isCancelled = true

        BlockBreakingSystem().MODIFIERS.add(getBreakModifier())

        block.world.playSound(block.location, blockyInfo.breakSound, 1.0f, 0.8f)
        isDropItems = false
        handleBlockyDrops(block, player)
        getBreakModifier()
    }

    private fun updateAndCheck(loc: Location) {
        val block = loc.add(0.0, 1.0, 0.0).block
        if (block.type == Material.NOTE_BLOCK) block.state.update(true, true)
        val nextBlock = block.location.add(0.0, 1.0, 0.0)
        if (nextBlock.block.type == Material.NOTE_BLOCK) updateAndCheck(block.location)
    }

    private fun getBreakModifier(): BlockHardnessModifiers {
        return object : BlockHardnessModifiers{
            override fun isTriggered(player: Player, block: Block, tool: ItemStack): Boolean {
                if (block.type != Material.NOTE_BLOCK) return false
                return block.getPrefabFromBlock() != null
            }

            override fun breakBlocky(player: Player, block: Block, tool: ItemStack) {
                block.type = Material.AIR
            }

            override fun getBreakTime(player: Player, block: Block, tool: ItemStack): Long {
                val prefab = block.getPrefabFromBlock() ?: return 0L
                val info = prefab.get<BlockyInfo>() ?: return 0L
                val period: Long = info.blockBreakTime.toLong()
                val modifier = 1.0
                return (period * modifier).toLong()
            }
        }
    }
}