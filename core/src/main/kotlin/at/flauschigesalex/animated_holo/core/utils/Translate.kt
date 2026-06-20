package at.flauschigesalex.animated_holo.core.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.*

internal object Translate {
    
    fun translate(key: String, locale: Locale): String = runCatching {
        
        require(key.isNotEmpty()) { "Key must not be empty!" }

        val bundle = ResourceBundle.getBundle(
            "i18n/messages",
            locale,
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
        )
        
        return bundle.getString(key)
    }.getOrNull() ?: "?($key)"
}

val Audience.locale: Locale
    get() = (this as? Player)?.locale() ?: Locale.getDefault()

fun Audience.getTranslated(key: String, vararg args: Any?, prefix: Boolean = false, richConsumer: Audience.(String) -> String = { it }): Component {
    val translation = Translate.translate(key, this.locale)
    val richTranslation = richConsumer.invoke(this, translation)
    
    val prefix = if (prefix) "<dark_gray>› <gray>" else "<gray>"
    val formatted = richTranslation.format(*args)
    
    return MiniMessage.miniMessage().deserialize("${prefix}${formatted}")
}

fun Audience.sendTranslated(key: String, vararg args: Any?, richConsumer: Audience.(String) -> String = { it }) {
    this.sendMessage(this.getTranslated(key, *args, true, richConsumer))
}
