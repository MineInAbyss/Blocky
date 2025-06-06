package com.mineinabyss.blocky

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.blocky.helpers.FurnitureOutlineType
import com.mineinabyss.blocky.menus.emptyItemModel
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.level.block.Block
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
@Serializable
data class BlockyConfig(
    val menus: BlockyMenus = BlockyMenus(),
    @YamlComment("[restoreFunctionality]: Restores redstone and vanilla note functionality for noteblocks", "NOTE: This reduces the total amount of custom block slots from 799 to 750")
    val noteBlocks: BlockyNoteBlockConfig = BlockyNoteBlockConfig(),
    val tripWires: BlockyTripwireConfig = BlockyTripwireConfig(),
    val furniture: BlockyFurnitureConfig = BlockyFurnitureConfig(),
    @YamlComment("You will need to disable these in world-generation if you wanna use it", "It aims to free up all CAVE_VINES except age = 0 and replaces its behaviour with CAVE_VINES_PLANT")
    val caveVineBlocks: BlockyCaveVineConfig = BlockyCaveVineConfig(),
    val slabBlocks: BlockySlabConfig = BlockySlabConfig(),
    val stairBlocks: BlockyStairConfig = BlockyStairConfig(),
    val doorBlocks: BlockyDoorConfig = BlockyDoorConfig(),
    val trapdoorBlocks: BlockyTrapDoorConfig = BlockyTrapDoorConfig(),
    val grateBlocks: BlockyGrateConfig = BlockyGrateConfig(),
    @YamlComment("If you wish to use vanilla sounds, set this to true", "Keep in mind it disables all custom sounds from place/break/hit etc")
    val disableCustomSounds: Boolean = false,
) {

    @Serializable
    data class BlockyMenus(
        val defaultMenu: DefaultBlockyMenu = DefaultBlockyMenu(),
        val blockMenu: BlockyMenu = BlockyMenu(),
        val wireMenu: BlockyMenu = BlockyMenu(),
        val furnitureMenu: BlockyMenu = BlockyMenu(),
    )

    @Serializable data class BlockyMenu(val title: @Serializable(MiniMessageSerializer::class) Component = Component.empty(), val height: Int = 6)
    @Serializable data class BlockyNoteBlockConfig(val isEnabled: Boolean = true, val restoreVanillaFunctionality: Boolean = false)
    @Serializable data class BlockyTripwireConfig(val isEnabled: Boolean = true)
    @Serializable data class BlockyCaveVineConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockySlabConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyStairConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyDoorConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyTrapDoorConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyGrateConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyFurnitureConfig(val hitboxOutlines: HitboxOutline = HitboxOutline()) {
        @Transient val showOutlines = hitboxOutlines.type != FurnitureOutlineType.NONE

        @Serializable
        data class HitboxOutline(
            @YamlComment("Valid typed are ITEM, BLOCK, NONE")
            val type: FurnitureOutlineType = FurnitureOutlineType.ITEM,
            val item: SerializableItemStack = ItemStack(Material.PAPER).toSerializable()
        ) {
            fun entityType(): net.minecraft.world.entity.EntityType<*>? {
                return when (type) {
                    FurnitureOutlineType.ITEM -> net.minecraft.world.entity.EntityType.ITEM_DISPLAY
                    FurnitureOutlineType.BLOCK -> net.minecraft.world.entity.EntityType.BLOCK_DISPLAY
                    else -> null
                }
            }
            fun outlineContent(): SynchedEntityData.DataValue<*>? {
                return when (type) {
                    FurnitureOutlineType.ITEM ->
                        SynchedEntityData.DataValue(23, EntityDataSerializers.ITEM_STACK, CraftItemStack.asNMSCopy(item.toItemStack()))
                    FurnitureOutlineType.BLOCK ->
                        SynchedEntityData.DataValue(23, EntityDataSerializers.BLOCK_STATE, Block.byItem(CraftItemStack.asNMSCopy(item.toItemStack()).item).defaultBlockState())
                    else -> null
                }
            }
        }
    }
    @Serializable data class DefaultBlockyMenu(
        val title: @Serializable(MiniMessageSerializer::class) Component = Component.empty(),
        val height: Int = 5,
        val blockButton: SerializableItemStack = ItemStack(Material.PAPER).apply {
            setData(DataComponentTypes.ITEM_NAME, "<gradient:gold:yellow>Block Menu".miniMsg())
            setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
        }.toSerializable(),
        val wireButton: SerializableItemStack = ItemStack(Material.PAPER).apply {
            setData(DataComponentTypes.ITEM_NAME, "<gradient:gold:yellow>Wire Menu".miniMsg())
            setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
        }.toSerializable(),
        val furnitureButton: SerializableItemStack = ItemStack(Material.PAPER).apply {
            setData(DataComponentTypes.ITEM_NAME, "<gradient:gold:yellow>Furniture Menu".miniMsg())
            setData(DataComponentTypes.ITEM_MODEL, emptyItemModel)
        }.toSerializable(),
    )
}
