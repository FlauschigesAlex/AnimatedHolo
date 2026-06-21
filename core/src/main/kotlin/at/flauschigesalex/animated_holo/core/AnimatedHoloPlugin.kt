package at.flauschigesalex.animated_holo.core

import at.flauschigesalex.animated_holo.core.external_api.PlaceholderIntegration
import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.core.holo.animation.AnimationListener
import at.flauschigesalex.lib.minecraft.paper.base.FlauschigeLibraryPaper
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused", "UNUSED_EXPRESSION")
class AnimatedHoloPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: AnimatedHoloPlugin
            private set
    }
    
    override fun onEnable() {
        instance = this
        FlauschigeLibraryPaper.init(this, javaClass.packageName)

        AnimationListener // Load Animations
        Configuration // Load Configuration
        Holograms // Load Holograms
        CommandRegister // Register Commands
        PlaceholderIntegration.enable(this) // Enable Packet Listener
    }
}