package at.flauschigesalex.animated_holo.core.holo.animation

import at.flauschigesalex.animated_holo.core.AnimatedHoloPlugin
import at.flauschigesalex.animated_holo.core.Configuration
import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.lib.holo.animation.AnimationListener as IAnimationListener
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

@Suppress("unused", "UNUSED_EXPRESSION")
internal object AnimationListener : IAnimationListener {
    
    init {
        HologramAnimationImpl // Load Animations
        if (Configuration.useAnimations) this.enableAnimations()
    }

    private var task: BukkitTask? = null
    override fun enableAnimations() {
        if (task != null) return
        task = Bukkit.getScheduler().runTaskTimer(AnimatedHoloPlugin.instance, Runnable {
            Holograms.entities.forEach { (config, entity) ->
                val animation = config.animation ?: return@forEach
                if (animation.shouldInvoke(Bukkit.getCurrentTick()).not()) return@forEach
                animation.invoke(config, entity)
            }
        }, 0, 1)
    }
    
    override fun disableAnimations() {
        task?.cancel()
        task = null
    }
}