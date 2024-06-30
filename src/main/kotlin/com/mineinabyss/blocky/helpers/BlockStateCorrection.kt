package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialSetTag
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import org.bukkit.GameMode
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

object BlockStateCorrection {
    fun placeItemAsBlock(player: Player, slot: EquipmentSlot, itemStack: ItemStack)  {
        val placedItem = itemStack.takeIf(CopperHelpers::isBlockyCopper)?.let(CopperHelpers::convertToBlockyType) ?: CopperHelpers.convertToFakeType(itemStack)
        val nmsStack = CraftItemStack.asNMSCopy(placedItem)
        val blockItem = nmsStack.item as? BlockItem
        val serverPlayer = (player as CraftPlayer).handle
        val hitResult = playerPOVHitResult(serverPlayer)
        val hand = if (slot == EquipmentSlot.HAND) InteractionHand.MAIN_HAND else InteractionHand.OFF_HAND

        val placeContext = when {// Shulker-Boxes are DirectionalPlace based unlike other directional-blocks
            MaterialSetTag.SHULKER_BOXES.isTagged(placedItem.type) ->
                DirectionalPlaceContext(serverPlayer.level(), hitResult.blockPos, hitResult.direction, nmsStack, hitResult.direction.opposite)
            else -> BlockPlaceContext(UseOnContext(serverPlayer, hand, hitResult))
        }

        blockItem?.let {
            if (blockItem.place(placeContext) == InteractionResult.FAIL) return
            // Seems shulkers for some reason do not adhere to the place-item subtraction by default
            if (placeContext is DirectionalPlaceContext && player.getGameMode() != GameMode.CREATIVE)
                placedItem.subtract(1)
            val target = hitResult.blockPos.let { pos -> player.world.getBlockAt(pos.x, pos.y, pos.z) }
            // Open sign, side will always be front when placed
            (target.state as? Sign)?.let { if (!it.isWaxed) player.openSign(it, Side.FRONT) }
        } ?: serverPlayer.gameMode.useItem(serverPlayer, serverPlayer.level(), nmsStack, hand)
    }

    private fun playerPOVHitResult(player: net.minecraft.world.entity.player.Player, fluidHandling: ClipContext.Fluid = ClipContext.Fluid.ANY): BlockHitResult {
        val f = player.xRot
        val g = player.yRot
        val vec3 = player.eyePosition
        val h = Mth.cos(-g * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val i = Mth.sin(-g * (Math.PI.toFloat() / 180f) - Math.PI.toFloat())
        val j = -Mth.cos(-f * (Math.PI.toFloat() / 180f))
        val k = Mth.sin(-f * (Math.PI.toFloat() / 180f))
        val l = i * j
        val n = h * j
        val d = 5.0
        val vec32 = vec3.add(l.toDouble() * d, k.toDouble() * d, n.toDouble() * d)
        return player.level().clip(ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluidHandling, player))
    }
}
