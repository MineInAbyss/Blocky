package com.mineinabyss.blocky

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.Serializable
import org.bukkit.Material
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
    @YamlComment("This is for generating the Json files needed for MoreCreativeTabs", "A mod that lets you customize the creative tabs in Minecraft", "This will generate a resource-pack that will include tabs for all Blocky Blocks and Furniture")
    val MoreCreativeTabsMod: MoreCreativeTabsModConfig = MoreCreativeTabsModConfig(),
) {

    @Serializable
    data class BlockyMenus(
        val defaultMenu: DefaultBlockyMenu = DefaultBlockyMenu(),
        val blockMenu: BlockyMenu = BlockyMenu(),
        val wireMenu: BlockyMenu = BlockyMenu(),
        val furnitureMenu: BlockyMenu = BlockyMenu(),
    )

    @Serializable data class BlockyMenu(val title: String = "", val height: Int = 6)
    @Serializable data class MoreCreativeTabsModConfig(val generateJsonForMod: Boolean = false)
    @Serializable data class BlockyNoteBlockConfig(val isEnabled: Boolean = true)
    @Serializable data class BlockyTripwireConfig(val isEnabled: Boolean = true)
    @Serializable data class BlockyCaveVineConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockySlabConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyStairConfig(val isEnabled: Boolean = false)
    @Serializable data class BlockyFurnitureConfig(val showHitboxOutline: Boolean = false)
    @Serializable data class DefaultBlockyMenu(
        val title: String = "",
        val height: Int = 5,
        val blockButton: SerializableItemStack = ItemStack(Material.PAPER).editItemMeta { setCustomModelData(1); displayName("<gradient:gold:yellow>Block Menu".miniMsg()) }.toSerializable(),
        val wireButton: SerializableItemStack = ItemStack(Material.PAPER).editItemMeta { setCustomModelData(1); displayName("<gradient:gold:yellow>Wire Menu".miniMsg()) }.toSerializable(),
        val furnitureButton: SerializableItemStack = ItemStack(Material.PAPER).editItemMeta { setCustomModelData(1); displayName("<gradient:gold:yellow>Furniture Menu".miniMsg()) }.toSerializable(),
    )
}
