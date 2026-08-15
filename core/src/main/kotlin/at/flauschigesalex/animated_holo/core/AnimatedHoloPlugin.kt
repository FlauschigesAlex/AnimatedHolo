package at.flauschigesalex.animated_holo.core

import at.flauschigesalex.animated_holo.core.external_api.PlaceholderIntegration
import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.core.holo.animation.AnimationListener
import at.flauschigesalex.animated_holo.core.utils.scheduleAsync
import at.flauschigesalex.animated_holo.core.utils.sendTranslated
import at.flauschigesalex.lib.minecraft.paper.base.FlauschigeLibraryPaper
import at.flauschigesalex.rinth.project.version.MProjectVersionDifference
import at.flauschigesalex.rinth.project.version.listener.PaperVersionUpdateListener
import at.flauschigesalex.rinth.project.version.onChanges
import at.flauschigesalex.rinth.utils.checker.version.VersionChecker
import org.bstats.bukkit.Metrics
import net.kyori.adventure.audience.Audience
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
        
        val metrics = Metrics(this, 33396)

        PaperVersionUpdateListener(this) { audience ->
            scheduleAsync {
                val changes = VersionChecker.check("AnimatedHolo", this.channel).currentVersionDiff(server).getOrNull() ?: return@scheduleAsync
                changes.onChanges {
                    audience.sendNewerVersionMessage(this)
                }
            }
        }
    }
}

internal fun Audience.sendNewerVersionMessage(changes: MProjectVersionDifference) {
    this.sendTranslated("version.update.line1", changes.newer.slug, "<gold>${changes.newer.version}</gold>") { "<yellow>$it" }
    this.sendTranslated("version.update.line2", "<red>${changes.older.version}</red>", "<yellow>${changes.indexDifference}</yellow>")
    this.sendTranslated("version.update.line3", "<green><u><click:open_url:'${changes.newer.downloadUrl}'>${changes.newer.downloadUrl}</click></u></green>")
}