package at.flauschigesalex.animated_holo.lib.data.position

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

@Serializable
data class Position(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
) {
}

fun Position.toLocation() = Location(Bukkit.getWorld(name), x, y, z, yaw, pitch)
fun Location.toPosition() = Position(this.world.name, x, y, z, yaw, pitch)
