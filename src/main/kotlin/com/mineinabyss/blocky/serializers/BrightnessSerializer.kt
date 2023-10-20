package com.mineinabyss.blocky.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.entity.Display.Brightness

@Serializable
@SerialName("Brightness")
private class BrightnessSurrogate(val blockLight: Int = 0, val skyLight: Int = 0)

object BrightnessSerializer : KSerializer<Brightness> {
    override val descriptor: SerialDescriptor = BrightnessSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Brightness) {
        encoder.encodeSerializableValue(
            BrightnessSurrogate.serializer(),
            BrightnessSurrogate(value.blockLight, value.skyLight)
        )
    }

    override fun deserialize(decoder: Decoder): Brightness {
        val surrogate = decoder.decodeSerializableValue(BrightnessSurrogate.serializer())
        return Brightness(surrogate.blockLight, surrogate.skyLight)
    }
}
