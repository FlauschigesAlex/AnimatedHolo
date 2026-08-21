package at.flauschigesalex.animated_holo.lib.holo.animation.list

import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.animated_holo.lib.holo.attributes.AnimationModifierAttribute
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Vector3f

@Suppress("unused")
object JumpingAnimation : HologramAnimation<JumpingData>(
    JumpingData(0)
) {
    private const val ANTICIPATION_DURATION = 12
    private const val CHARGE_HOLD_DURATION = 4
    private const val JUMP_DURATION = 31
    private const val CYCLE_DURATION = 54
    private const val ANTICIPATION_DEPTH = 22f
    private const val JUMP_HEIGHT = 140f

    override fun invoke(holo: HologramConfiguration, entity: TextDisplay) {
        holo.editData {
            val modifier = holo.getAttribute(AnimationModifierAttribute::class.java)?.value ?: 1f
            val offset = when {
                it.step < ANTICIPATION_DURATION -> {
                    val progress = it.step / (ANTICIPATION_DURATION - 1f)
                    val easedProgress = progress * progress * (3f - 2f * progress)
                    -ANTICIPATION_DEPTH * easedProgress
                }

                it.step < ANTICIPATION_DURATION + CHARGE_HOLD_DURATION ->
                    -ANTICIPATION_DEPTH

                it.step < ANTICIPATION_DURATION + CHARGE_HOLD_DURATION + JUMP_DURATION -> {
                    val jumpStep = it.step - ANTICIPATION_DURATION - CHARGE_HOLD_DURATION
                    val progress = jumpStep / (JUMP_DURATION - 1f)

                    if (progress <= .5f) {
                        val easedProgress = smoothStep(progress * 2f)
                        -ANTICIPATION_DEPTH + (JUMP_HEIGHT + ANTICIPATION_DEPTH) * easedProgress
                    } else {
                        val easedProgress = smoothStep((progress - .5f) * 2f)
                        JUMP_HEIGHT * (1f - easedProgress)
                    }
                }

                else -> 0f
            }

            val transformation = entity.transformation
            val vector = Vector3f(0f, offset * modifier / 100f, 0f)

            entity.transformation = Transformation(
                vector,
                transformation.leftRotation,
                transformation.scale, 
                transformation.rightRotation,
            )
            
            return@editData it.copy(step = (it.step + 1) % CYCLE_DURATION)
        }
    }

    private fun smoothStep(progress: Float): Float =
        progress * progress * (3f - 2f * progress)
}

@ConsistentCopyVisibility
data class JumpingData internal constructor(val step: Int)
