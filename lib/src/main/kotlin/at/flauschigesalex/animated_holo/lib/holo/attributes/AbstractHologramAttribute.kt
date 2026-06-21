package at.flauschigesalex.animated_holo.lib.holo.attributes

import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.base.general.Reflector
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(HologramAttributeSerializer::class)
sealed interface HologramAttribute {
    companion object {
        var DEFAULT_ATTRIBUTES = setOf(
            HoverScaleAttribute(1.5f),
            HoverTransitionAttribute(3f),
        )

        val entries: Set<Class<out HologramAttribute>> = Reflector.reflect(javaClass.classLoader, javaClass.packageName).getSubTypes(HologramAttribute::class.java).toSet()
    }

    val value: Float
}

internal object HologramAttributeSerializer : KSerializer<HologramAttribute> {
    override val descriptor: SerialDescriptor = JsonManager.serializer().descriptor

    override fun serialize(encoder: Encoder, value: HologramAttribute) {
        val json = JsonManager(
            "type" to value.javaClass.simpleName,
            "value" to value.value
        )
        encoder.encodeSerializableValue(JsonManager.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): HologramAttribute {
        val json = decoder.decodeSerializableValue(JsonManager.serializer())

        val typeRaw = json.getString("type")
        val type = HologramAttribute.entries.find { it.simpleName == typeRaw }!!
        
        val constructor = type.getConstructor(Float::class.java)
        constructor.isAccessible = true
        
        val value = json.getFloat("value")!!

        return constructor.newInstance(value) as HologramAttribute
    }
}