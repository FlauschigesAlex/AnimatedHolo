package at.flauschigesalex.animated_holo.core.holo.animation

import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.lib.base.general.Reflector
import java.lang.reflect.Modifier

object HologramAnimationImpl {
    init {
        val entries = Reflector.reflect(javaClass.classLoader, javaClass.packageName).getSubTypes(HologramAnimation::class.java)
            .filterNot {
                Modifier.isAbstract(it.modifiers)
            }.mapNotNull { clazz ->
                clazz.kotlin.objectInstance?.let {
                    return@mapNotNull it
                }

                return@mapNotNull runCatching {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.isAccessible = true

                    return@runCatching constructor.newInstance() as HologramAnimation<*>
                }.getOrNull()
            }.toSet()

        // Register animations
        HologramAnimation.entries.addAll(entries)
    }
}