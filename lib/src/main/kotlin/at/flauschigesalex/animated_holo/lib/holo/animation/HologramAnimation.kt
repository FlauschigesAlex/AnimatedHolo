package at.flauschigesalex.animated_holo.lib.holo.animation

import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.entity.TextDisplay
import kotlin.jvm.javaClass

@Suppress("unused")
@Serializable(AnimationSerializer::class)
abstract class HologramAnimation<AD: Any>(protected val defaultData: AD) {
    
    companion object {
        val entries = mutableSetOf<HologramAnimation<*>>()
    }

    private val savedData = mutableMapOf<HologramConfiguration, AD>()
    protected fun HologramConfiguration.data() = savedData.getOrElse(this) { defaultData }
    protected fun HologramConfiguration.editData(data: AD) {
        savedData[this] = data
    }
    protected fun HologramConfiguration.editData(consumer: (AD) -> AD) {
        savedData[this] = consumer(this.data())
    }
    
    open fun shouldInvoke(currentTick: Int): Boolean = true
    abstract operator fun invoke(holo: HologramConfiguration, entity: TextDisplay)
}

internal object AnimationSerializer : KSerializer<HologramAnimation<*>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Animation", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: HologramAnimation<*>) {
        encoder.encodeString(value.javaClass.simpleName)
    }

    override fun deserialize(decoder: Decoder): HologramAnimation<*> {
        val animationName = decoder.decodeString()
        return HologramAnimation.entries.find { it.javaClass.simpleName == animationName }
            ?: error("Unknown animation: $animationName")
    }
}
