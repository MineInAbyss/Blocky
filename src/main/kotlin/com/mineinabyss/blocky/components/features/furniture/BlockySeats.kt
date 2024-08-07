package com.mineinabyss.blocky.components.features.furniture

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.VectorSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import org.bukkit.util.Vector

@Serializable(BlockySeats.Serializer::class)
class BlockySeats(val offsets: Set<@Serializable(VectorSerializer::class) Vector> = emptySet()) {
    class Serializer : InnerSerializer<Set<Vector>, BlockySeats>(
        serialName = "blocky:seats",
        inner = SetSerializer(VectorSerializer),
        inverseTransform = { it.offsets },
        transform = { BlockySeats(it) }
    )
}
