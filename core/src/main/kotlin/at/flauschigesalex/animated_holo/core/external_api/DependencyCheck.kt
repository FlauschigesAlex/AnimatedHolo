package at.flauschigesalex.animated_holo.core.external_api

import at.flauschigesalex.animated_holo.core.AnimatedHoloPlugin
import at.flauschigesalex.animated_holo.core.Configuration
import com.comphenix.protocol.ProtocolLibrary
import me.clip.placeholderapi.PlaceholderAPI
import java.util.logging.Logger

object DependencyCheck {
    
    private val _missing = mutableSetOf<Dependency>()
    val missing: Set<Dependency> get() = _missing.toSet()
    
    @Deprecated("Dependency requirements cannot be empty.", level = DeprecationLevel.ERROR)
    fun require(block: () -> Unit) {
        throw UnsupportedOperationException()
    }
    
    fun require(dependency: Dependency, block: () -> Unit) = require(listOf(dependency), block)
    fun require(vararg dependency: Dependency, block: () -> Unit) = require(dependency.toList(), block)
    fun require(dependency: List<Dependency>, block: () -> Unit) {
        val missing = dependency.filterNot { it.isPresent }
        if (missing.isNotEmpty()) {
            missing.forEach { if (_missing.add(it) && !Configuration.dependencyMissingNoWarn.contains(it.name)) it.onMissing(AnimatedHoloPlugin.instance.logger) }
            return
        }
        
        block()
    }
}

enum class Dependency(check: () -> Boolean, internal val onMissing: (Logger) -> Unit) {
    PROTOCOL_LIB({
        runCatching { ProtocolLibrary.getProtocolManager() }.isSuccess
    }, { logger ->
        // TODO
        logger.warning("TODO SEND TRANSLATED PROTOCOL LIB MISSING MESSAGE")
    }),
    PLACEHOLDER_API({
        runCatching { PlaceholderAPI.getPlaceholderPattern() }.isSuccess
    }, { logger ->
        // TODO
        logger.warning("TODO SEND TRANSLATED PLACEHOLDER API MISSING MESSAGE")
    }),
    ;
    
    val isPresent = check()
}
