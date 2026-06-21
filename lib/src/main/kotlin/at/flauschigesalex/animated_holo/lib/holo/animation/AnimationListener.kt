package at.flauschigesalex.animated_holo.lib.holo.animation

@Suppress("unused")
interface AnimationListener {

    /**
     * Enables the task for hologram animations.<br>
     * Toggling this will not affect the configuration.
     */
    fun enableAnimations()
    
    /**
     * Disables the task for hologram animations.<br>
     * Toggling this will not affect the configuration.
     */
    fun disableAnimations()
}