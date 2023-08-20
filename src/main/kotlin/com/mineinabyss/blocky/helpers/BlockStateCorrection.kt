package com.mineinabyss.blocky.helpers

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.blocky.helpers.GenericHelpers.getRelativeFacing
import com.mineinabyss.idofront.nms.aliases.toNMS
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.bukkit.Axis
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.*
import org.bukkit.block.Sign
import org.bukkit.block.data.*
import org.bukkit.block.data.type.*
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.Lectern
import org.bukkit.block.sign.Side
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.SkullMeta

object BlockStateCorrection {

    fun placeItemAsBlock(player: Player, slot: EquipmentSlot, itemStack: ItemStack, placedAgainst: Block) {
        val blockHitResult = getBlockHitResult(player, placedAgainst) ?: return
        val hand = when (slot) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }

        if (Tag.STAIRS.isTagged(itemStack.type) || Tag.SLABS.isTagged(itemStack.type))
            handleHalfBlocks(itemStack.type.createBlockData(), player)
        else itemStack.toNMS()?.useOn(UseOnContext(player.toNMS(), hand, blockHitResult))
    }

    private fun getBlockHitResult(player: Player, block: Block): BlockHitResult? {
        val human = (player as? CraftPlayer)?.handle ?: return null
        val loc = player.eyeLocation
        return BlockHitResult(Vec3(loc.x, loc.y, loc.z), human.direction.opposite, block.toBlockPos(), false)
    }

    //TODO This might be better to call via an event or something instead of this god awful method
    // Even NMS might be better as nmsItem.place is a method
    fun correctAllBlockStates(block: Block, player: Player, face: BlockFace, item: ItemStack): Boolean {
        val data = block.blockData.clone()
        val state = block.state
        val booleanChecks = booleanChecks(block, face, item)
        if (state is Skull && item.itemMeta is SkullMeta) {
            (item.itemMeta as SkullMeta).playerProfile?.let { state.setPlayerProfile(it) }
            state.update(true, false)
        }
        if (booleanChecks != null) return booleanChecks
        if (data !is Door && (data is Bisected || data is Slab)) block.handleHalfBlocks(player)
        if (data is Rotatable) block.handleRotatableBlocks(player)
        if (MaterialTags.CORAL_FANS.isTagged(block) && face != BlockFace.UP)
            block.type = Material.valueOf(block.type.toString().replace("_CORAL_FAN", "_CORAL_WALL_FAN"))
        if (data is Waterlogged) block.handleWaterlogged(face)
        if (data is Ageable) {
            return if ((block.type == Material.WEEPING_VINES || block.type == Material.WEEPING_VINES_PLANT) && face != BlockFace.DOWN) false
            else if ((block.type == Material.TWISTING_VINES || block.type == Material.TWISTING_VINES_PLANT) && face != BlockFace.UP) false
            else false
        }
        if ((data is Door || data is Bed || data is Chest || data is Bisected) && data !is Stairs && data !is TrapDoor)
            if (!block.handleDoubleBlocks(player)) return false
        if ((state is Skull || state is Sign || MaterialTags.TORCHES.isTagged(block)) && face != BlockFace.DOWN && face != BlockFace.UP)
            block.handleWallAttachable(player, face)

        if (data !is Stairs && (data is Directional || data is FaceAttachable || data is MultipleFacing || data is Attachable)) {
            block.handleDirectionalBlocks(face)
        }

        if (data is Orientable) {
            data.axis = when {
                (face == BlockFace.UP || face == BlockFace.DOWN) -> Axis.Y
                (face == BlockFace.NORTH || face == BlockFace.SOUTH) -> Axis.Z
                (face == BlockFace.WEST || face == BlockFace.EAST) -> Axis.X
                else -> Axis.Y
            }
            block.setBlockData(data, false)
        }

        if (data is Lantern) {
            if (face != BlockFace.DOWN) return false
            data.isHanging = true
            block.setBlockData(data, false)
        }

        if (data is Repeater) {
            data.facing = player.facing.oppositeFace
            block.setBlockData(data, false)
        }

        if (Tag.ANVIL.isTagged(block.type)) {
            (data as Directional).facing = GenericHelpers.getAnvilFacing(face)
            block.setBlockData(data, false)
        }

        if (data is Lectern) {
            data.facing = player.facing.oppositeFace
            block.setBlockData(data, false)
        }

        if (state is BlockInventoryHolder && ((item.itemMeta as BlockStateMeta).blockState is BlockInventoryHolder)) {
            ((item.itemMeta as BlockStateMeta).blockState as Container).inventory.forEach { i ->
                if (i != null) state.inventory.addItem(i)
            }
        }

        if (state is Sign) player.openSign(state, Side.FRONT)

        return true
    }

    private fun Block.handleWaterlogged(face: BlockFace) {
        val data = blockData.clone()
        when (data) {
            is Waterlogged -> {
                if (data is Directional && data !is Stairs) data.facing = face
                data.isWaterlogged = false
            }
        }
        setBlockData(data, false)
    }

    private fun Block.handleWallAttachable(player: Player, face: BlockFace) {
        if (state is Sign) player.openSign(state as Sign, Side.FRONT)
        type =
            if (MaterialTags.TORCHES.isTagged(this))
                Material.valueOf(type.toString().replace("TORCH", "WALL_TORCH"))
            else if (Tag.STANDING_SIGNS.isTagged(type))
                Material.valueOf(type.toString().replace("_SIGN", "_WALL_SIGN"))
            else if (type.toString().endsWith("SKULL") && !type.toString().endsWith("_WALL_SKULL"))
                Material.valueOf(type.toString().replace("_SKULL", "_WALL_SKULL"))
            else Material.valueOf(type.toString().replace("_HEAD", "_WALL_HEAD"))

        val data = type.createBlockData() as Directional
        data.facing = face
        setBlockData(data, false)
    }

    private fun Block.handleDoubleBlocks(player: Player): Boolean {
        when (val blockData = blockData) {
            is Door -> {
                if (getRelative(BlockFace.UP).type.isSolid || !getRelative(BlockFace.UP).isReplaceable) return false
                if (GenericHelpers.getLeftBlock(this, player).blockData is Door)
                    blockData.hinge = Door.Hinge.RIGHT
                else blockData.hinge = Door.Hinge.LEFT

                blockData.facing = player.facing
                blockData.half = Bisected.Half.TOP
                getRelative(BlockFace.UP).setBlockData(blockData, false)
                blockData.half = Bisected.Half.BOTTOM

                setBlockData(blockData, false)
            }

            is Bed -> {
                if (getRelative(player.facing).type.isSolid || !getRelative(player.facing).isReplaceable) return false
                getRelative(player.facing).setBlockData(blockData, false)
                val nextBlock = getRelative(player.facing)
                val nextData = nextBlock.blockData as Bed

                blockData.part = Bed.Part.FOOT
                nextData.part = Bed.Part.HEAD
                blockData.facing = player.facing
                nextData.facing = player.facing
                nextBlock.blockData = nextData
                setBlockData(blockData, false)
            }

            is Chest -> {
                if (GenericHelpers.getLeftBlock(this, player).blockData is Chest)
                    blockData.type = Chest.Type.LEFT
                else if (GenericHelpers.getRightBlock(this, player).blockData is Chest)
                    blockData.type = Chest.Type.RIGHT
                else blockData.type = Chest.Type.SINGLE

                blockData.facing = player.facing.oppositeFace
                setBlockData(blockData, true)
            }

            is Bisected -> {
                if (getRelative(BlockFace.UP).type.isSolid || !getRelative(BlockFace.UP).isReplaceable) return false

                blockData.half = Bisected.Half.TOP
                getRelative(BlockFace.UP).setBlockData(blockData, false)
                blockData.half = Bisected.Half.BOTTOM
            }

            else -> {
                setBlockData(Material.AIR.createBlockData(), false)
                return false
            }
        }
        return true
    }

    private fun handleHalfBlocks(data: BlockData, player: Player): BlockData {
        val eye = player.rayTraceBlocks(5.0, FluidCollisionMode.NEVER) ?: return data
        when (data) {
            is TrapDoor -> {
                data.facing = player.facing.oppositeFace
                if (eye.hitPosition.y <= eye.hitBlock?.location?.toCenterLocation()?.y!!) data.half =
                    Bisected.Half.BOTTOM
                else data.half = Bisected.Half.TOP
            }

            is Stairs -> {
                data.facing = player.facing
                when {
                    eye.hitBlockFace == BlockFace.UP -> data.half = Bisected.Half.BOTTOM
                    eye.hitBlockFace == BlockFace.DOWN -> data.half = Bisected.Half.TOP
                    eye.hitPosition.y < eye.hitBlock?.location?.clone()?.apply { y += 0.6 }?.y!! -> data.half =
                        Bisected.Half.BOTTOM
                    else -> data.half = Bisected.Half.TOP
                }
            }

            is Slab -> {
                when {
                    eye.hitBlockFace == BlockFace.UP -> data.type = Slab.Type.BOTTOM
                    eye.hitBlockFace == BlockFace.DOWN -> data.type = Slab.Type.TOP
                    eye.hitPosition.y < eye.hitBlock?.location?.clone()?.apply { y += 0.6 }?.y!! -> data.type =
                        Slab.Type.BOTTOM
                    else -> data.type = Slab.Type.TOP
                }
            }
        }
        return data
    }

    private fun Block.handleHalfBlocks(player: Player) {
        handleHalfBlocks(blockData, player)
    }

    private fun Block.handleRotatableBlocks(player: Player) {
        val data = blockData.clone() as Rotatable
        data.rotation =
            if (MaterialTags.SKULLS.isTagged(this)) player.getRelativeFacing()
            else player.getRelativeFacing().oppositeFace
        setBlockData(data, false)
    }

    private fun Block.handleDirectionalBlocks(face: BlockFace) {
        val data = blockData.clone()

        when (data) {
            is Directional -> {
                if (data is FaceAttachable) {
                    when (face) {
                        BlockFace.UP -> data.attachedFace = FaceAttachable.AttachedFace.FLOOR
                        BlockFace.DOWN -> data.attachedFace = FaceAttachable.AttachedFace.CEILING
                        else -> {
                            data.facing = face
                        }
                    }
                } else data.facing = face
            }

            is MultipleFacing -> {
                data.allowedFaces.forEach {
                    if (getRelative(it).type.isSolid) data.setFace(it, true)
                    else data.setFace(it, false)
                }
            }

            is Attachable -> {
                data.isAttached = true
            }
        }
        setBlockData(data, false)
    }

    private fun Block.isCoralNotBlock() = MaterialTags.CORAL.isTagged(this) || MaterialTags.CORAL_FANS.isTagged(this)


    private fun booleanChecks(block: Block, face: BlockFace, item: ItemStack): Boolean? {
        return when {
            block.blockData is CaveVines || block.blockData is Tripwire || block.type == Material.CHORUS_PLANT -> true
            block.blockData is Sapling && face != BlockFace.UP -> false
            block.blockData is Ladder && (face == BlockFace.UP || face == BlockFace.DOWN) -> false
            block.type == Material.HANGING_ROOTS && face != BlockFace.DOWN -> false
            MaterialTags.TORCHES.isTagged(item) && face == BlockFace.DOWN -> false
            block.state is Sign && face == BlockFace.DOWN -> false
            block.isCoralNotBlock() && face == BlockFace.DOWN -> false
            MaterialTags.CORAL.isTagged(block) && block.getRelative(BlockFace.DOWN).type == Material.AIR -> false
            block.blockData is MultipleFacing && block.blockData !is GlassPane && face == BlockFace.UP -> false
            block.blockData is CoralWallFan && face == BlockFace.DOWN -> false
            else -> null
        }
    }

}
