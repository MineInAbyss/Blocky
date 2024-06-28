package com.mineinabyss.blocky

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.blocky.helpers.FurnitureOutlineType
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.ticxo.modelengine.api.entity.Hitbox
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.kyori.adventure.text.Component
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

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
    @Serializable data class BlockyFurnitureConfig(
        val hitboxOutlines: HitboxOutline = HitboxOutline(),
        val worldEdit: Boolean = false
    ) {
        @Transient val showOutlines = hitboxOutlines.type != FurnitureOutlineType.NONE

        @Serializable
        data class HitboxOutline(
            @YamlComment("Valid typed are ITEM, BLOCK, NONE")
            val type: FurnitureOutlineType = FurnitureOutlineType.ITEM,
            val item: SerializableItemStack = SerializableItemStack(type = Material.PAPER)
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
        val blockButton: SerializableItemStack = SerializableItemStack(type = Material.PAPER, customModelData = 1, itemName = "<gradient:gold:yellow>Block Menu".miniMsg()),
        val wireButton: SerializableItemStack = SerializableItemStack(type = Material.PAPER, customModelData = 1, itemName = "<gradient:gold:yellow>Wire Menu".miniMsg()),
        val furnitureButton: SerializableItemStack = SerializableItemStack(type = Material.PAPER, customModelData = 1, itemName = "<gradient:gold:yellow>Furniture Menu".miniMsg()),
    )
}
