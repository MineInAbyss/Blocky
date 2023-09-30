package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialSetTag
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.sign.Side
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object BlockStateCorrection {
    fun placeItemAsBlock(player: Player, slot: EquipmentSlot, itemStack: ItemStack, block: Block, blockFace: BlockFace): BlockData? {
        val hitResult = getBlockHitResult(player, block, blockFace) ?: return null
        val hand = when (slot) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> null
        } ?: return null

        val nmsStack = CraftItemStack.asNMSCopy(itemStack)
        val blockItem = nmsStack.item as? BlockItem
        val serverPlayer = (player as CraftPlayer).handle
        val placeContext = when {// Shulker-Boxes are DirectionalPlace based unlike other directional-blocks
            MaterialSetTag.SHULKER_BOXES.isTagged(itemStack.type) ->
                DirectionalPlaceContext(serverPlayer.level(), hitResult.blockPos, hitResult.direction, nmsStack, hitResult.direction.opposite)
            else -> BlockPlaceContext(UseOnContext(serverPlayer, hand, hitResult))
        }

        if (blockItem == null) {
            val result = nmsStack.item.use(serverPlayer.level(), serverPlayer, hand)
            if (result.result == InteractionResult.CONSUME) player.getInventory().setItem(slot, result.getObject().asBukkitCopy())
            return null
        }

        if (blockItem.place(placeContext) == InteractionResult.FAIL) return null
        // Seems shulkers for some reason do not adhere to the place-item subtraction by default
        if (placeContext is DirectionalPlaceContext && player.getGameMode() != GameMode.CREATIVE)
            itemStack.subtract(1)
        val target = hitResult.blockPos.let { pos -> block.world.getBlockAt(pos.x, pos.y, pos.z) }
        (target.state as? Sign)?.let { if (!it.isWaxed) player.openSign(it, Side.FRONT) }

        return target.blockData
    }

    private fun getBlockHitResult(player: Player, block: Block, blockFace: BlockFace): BlockHitResult? {
        val vec3 = player.eyeLocation.let { Vec3(it.x, it.y, it.z) }
        val direction = Direction.values().find { it.name == blockFace.name } ?: return null
        return BlockHitResult(vec3, direction.opposite, block.toBlockPos().relative(direction), false)
    }
}
