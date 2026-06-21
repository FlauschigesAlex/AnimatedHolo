package at.flauschigesalex.animated_holo.core.holo

import org.bukkit.entity.Player
import java.util.UUID

private val set = mutableSetOf<UUID>()

internal var Player.isHoloDebug: Boolean
    get() = set.contains(this.uniqueId)
    set(value) {
        if (value) set += this.uniqueId
        else set -= this.uniqueId
    }