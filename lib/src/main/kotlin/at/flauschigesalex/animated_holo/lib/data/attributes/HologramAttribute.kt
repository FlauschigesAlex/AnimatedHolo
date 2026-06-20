package at.flauschigesalex.animated_holo.lib.data.attributes

import at.flauschigesalex.lib.base.file.json.JsonManager
import at.flauschigesalex.lib.base.general.Reflector
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.jvm.jvmName

@Serializable(HologramAttributeSerializer::class)
sealed interface HologramAttribute {
    companion object {
        internal val DEFAULT_ATTRIBUTES = setOf(
            HoverScaleAttribute("1.2"),
            HoverTransitionAttribute("3"),
        )

        val entries: Set<Class<out HologramAttribute>> = Reflector.reflect(javaClass.classLoader, javaClass.packageName).getSubTypes(HologramAttribute::class.java).toSet()
    }

    val value: String

    fun isValid(): Boolean
    fun isNotValid(): Boolean = this.isValid().not()
}

internal object HologramAttributeSerializer : KSerializer<HologramAttribute> {
    override val descriptor: SerialDescriptor = JsonManager.serializer().descriptor

    override fun serialize(encoder: Encoder, value: HologramAttribute) {
        val json = JsonManager(
            "type" to value::class.jvmName,
            "value" to value.value
        )
        encoder.encodeSerializableValue(JsonManager.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): HologramAttribute {
        val json = decoder.decodeSerializableValue(JsonManager.serializer())

        val typeRaw = json.getString("type")
        val type = Class.forName(typeRaw)

        val constructor = type.getConstructor(String::class.java)
        return constructor.newInstance(json.getString("value")) as HologramAttribute
    }
}