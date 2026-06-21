package at.flauschigesalex.animated_holo.core.holo.animation.list

import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.animated_holo.lib.holo.attributes.AnimationModifierAttribute
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Vector3f

@Suppress("unused")
private class BobbingAnimation : HologramAnimation<BobbingData>(
    BobbingData(0)
) {

    override fun shouldInvoke(currentTick: Int): Boolean = currentTick %2 == 0
    
    override fun invoke(holo: HologramConfiguration, entity: TextDisplay) {
        holo.editData {
            var newInc = it.increment
            
            val newOffset = if (it.increment) it.offset +1 else it.offset -1
            if (newOffset >= 15) newInc = false
            if (newOffset <= -15) newInc = true
            
            val data = it.copy(offset = newOffset, increment = newInc)
            
            val transformation = entity.transformation
            val offset = data.offset * (holo.getAttribute(AnimationModifierAttribute::class.java)?.value ?: 1f)
            val vector = Vector3f(0f, (offset / 100f), 0f)

            entity.transformation = Transformation(
                vector,
                transformation.leftRotation,
                transformation.scale, 
                transformation.rightRotation,
            )
            
            return@editData data
        }
    }
}

private data class BobbingData(val offset: Int, val increment: Boolean = true)