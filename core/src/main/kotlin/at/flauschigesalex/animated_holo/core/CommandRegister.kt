package at.flauschigesalex.animated_holo.core

import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.core.holo.asTextDisplay
import at.flauschigesalex.animated_holo.core.holo.command.HologramAnimationArgumentType
import at.flauschigesalex.animated_holo.core.holo.command.HologramArgumentType
import at.flauschigesalex.animated_holo.core.holo.command.HologramAttributeArgumentType
import at.flauschigesalex.animated_holo.core.holo.isHoloDebug
import at.flauschigesalex.animated_holo.core.holo.remove
import at.flauschigesalex.animated_holo.core.utils.Colors
import at.flauschigesalex.animated_holo.core.utils.Permissions
import at.flauschigesalex.animated_holo.core.utils.Translate
import at.flauschigesalex.animated_holo.core.utils.sendConsoleDenied
import at.flauschigesalex.animated_holo.core.utils.sendTranslated
import at.flauschigesalex.animated_holo.core.utils.asRichString
import at.flauschigesalex.animated_holo.core.utils.locale
import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.animated_holo.lib.holo.attributes.HologramAttribute
import at.flauschigesalex.animated_holo.lib.holo.position.toLocation
import at.flauschigesalex.animated_holo.lib.holo.position.toPosition
import at.flauschigesalex.animated_holo.lib.utils.BukkitColor
import at.flauschigesalex.animated_holo.lib.utils.ColorArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.CommandBuilder
import at.flauschigesalex.lib.minecraft.brigadier.CommandContext
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.LiteralArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.BooleanArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.EnumArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.StringArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.DoubleArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.FloatArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.IntegerArgumentType
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

object CommandRegister {
    init {

        CommandBuilder("hologram") {
            this.alias("holo", "aholo", "ahologram")
            this.permission(Permissions.BASE)
            
            this.argument("create", LiteralArgumentType.literal()) {
                this.permission(Permissions.MODIFY)
                
                this.argument("name", StringArgumentType.string()) {
                    this.suggestions { _ ->
                        var number: Int? = null
                        
                        while (true) {
                            val holograms = Holograms
                            number = (number ?: holograms.count())
                            
                            val suggestedName = "animatedholo-${number}"
                            val names = holograms.map { it.id }
                            
                            if (names.any { it.equals(suggestedName, true) }) continue

                            return@suggestions setOf(suggestedName)
                        }
                        
                        return@suggestions setOf()
                    }
                    
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute context.sender.sendConsoleDenied()
                        val name = context.arguments.byType<String>()?.value ?: return@execute

                        if (Holograms.any { it.id.equals(name, true) }) {
                            sender.sendTranslated("hologram.create.failure.uniqueId", "${Colors.errorHighlight.asRichString()}$name</color>") {
                                "${Colors.error.asRichString()}${it}"
                            }
                            return@execute
                        }
                        
                        runCatching {
                            val holo = HologramConfiguration(name, sender.location.clone().let {
                                it.yaw = 0f
                                it.pitch = 0f
                                it.toPosition()
                            })
                            
                            Configuration.holograms += holo
                            holo.asTextDisplay()
                            
                            sender.sendTranslated("hologram.create",
                                "${Colors.highlight.asRichString()}${holo.id}</color>"
                            )
                            
                        }.onFailure { error ->
                            sender.sendTranslated("hologram.create.failure.unknown") {
                                "${Colors.error.asRichString()}${it}"
                            }
                            error.printStackTrace()
                        }
                    }

                    this.fail { it.sendInvalidSyntax() }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("near", LiteralArgumentType.literal().alias("list")) {
                this.permission(Permissions.READ)
                
                this.argument("distance", IntegerArgumentType.positive()) {
                    this.suggestions(setOf("10", "30", "50", "90"))
                    this.optional()
                    
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute context.sender.sendConsoleDenied()
                        val distance = context.arguments.byType<Int>()?.value ?: 20
                        
                        val holograms = Holograms.toMutableList()
                        holograms.removeIf { it.position.toLocation().distance(sender.location) > distance }
                        
                        val key = when (holograms.size) {
                            0 -> "hologram.near.none"
                            1 -> "hologram.near.single"
                            else -> "hologram.near.multiple"
                        }
                        
                        fun HologramConfiguration.toRichDisplay(): String {
                            return this.id
                        }
                        
                        sender.sendTranslated(key, "${Colors.highlight.asRichString()}${holograms.size}</color>", "${Colors.highlight.asRichString()}${distance}</color>") { text ->
                            text + holograms.joinToString { "<newline><reset> <dark_gray>› ${Colors.highlight.asRichString()}${it.toRichDisplay()}</color>" }
                        }
                    }

                    this.fail { it.sendInvalidSyntax() }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("move", LiteralArgumentType.literal()) {
                this.permission(Permissions.MODIFY)
                
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute context.sender.sendConsoleDenied()
                        val hologram = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                    
                        hologram.position = sender.location.toPosition()  
                        Configuration.updateHolo(hologram)
                        
                        sender.sendTranslated("hologram.move", "${Colors.highlight.asRichString()}${hologram.id}</color>")
                    }

                    this.fail { it.sendInvalidSyntax() }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("tp", LiteralArgumentType.literal()) {
                this.permission(Permissions.READ)
                
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute context.sender.sendConsoleDenied()
                        val hologram = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                    
                        sender.teleport(hologram.position.toLocation())
                        sender.sendTranslated("hologram.teleport", "${Colors.highlight.asRichString()}${hologram.id}</color>")
                    }

                    this.fail { it.sendInvalidSyntax() }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("edit", LiteralArgumentType.literal()) {
                this.permission(Permissions.MODIFY)
                
                this.argument("hologram", HologramArgumentType) {
                    this.argument("text", LiteralArgumentType.literal()) {
                        this.argument("addline", LiteralArgumentType.literal()) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.suggestions { context ->
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value
                                        ?: return@suggestions emptySet()
                                    val lines = holo.richLines.size

                                    return@suggestions (0 .. lines).map { it.toString() }.toSet()
                                }
                                this.optional()
                                
                                this.argument("text", GreedyArgumentType.greedy(StringArgumentType.string())) {
                                    this.execute { context ->
                                        val sender = context.sender
                                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                        val line = context.arguments.greedyByType<String>()?.value?.joinToString(" ") ?: return@execute
                                        
                                        val lastLine = holo.richLines.size
                                        val lineNum = context.arguments.byType<Int>()?.value ?: lastLine

                                        if (lineNum !in 0..lastLine) {
                                            sender.sendTranslated(
                                                "hologram.edit.text.line.failure.bounds",
                                                "${Colors.errorHighlight.asRichString()}$lineNum</color>",
                                                lastLine
                                            ) {
                                                "${Colors.error.asRichString()}$it"
                                            }
                                            return@execute
                                        }

                                        if (lineNum == lastLine) {
                                            holo.richLines.addLast(line)
                                            Configuration.updateHolo(holo)
                                            sender.sendTranslated("hologram.edit.text.line.add", "${Colors.highlight.asRichString()}${holo.id}</color>", "${Colors.highlight.asRichString()}${lastLine}</color>")
                                            return@execute
                                        }
                                        
                                        holo.richLines.add(lineNum, line)
                                        Configuration.updateHolo(holo)

                                        sender.sendTranslated("hologram.edit.text.line.add", "${Colors.highlight.asRichString()}${holo.id}</color>", "${Colors.highlight.asRichString()}${lineNum}</color>")
                                    }
                                }
                                
                                this.fail { it.sendInvalidSyntax() }
                            }

                            this.fail { it.sendInvalidSyntax() }
                        }
                        
                        this.argument("editline", LiteralArgumentType.literal().alias("setline")) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.suggestions { context ->
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value
                                        ?: return@suggestions emptySet()
                                    val lines = holo.richLines.size

                                    return@suggestions (0 ..< lines).map { it.toString() }.toSet()
                                }
                                
                                this.argument("text", GreedyArgumentType.greedy(StringArgumentType.string())) {
                                    this.execute { context ->
                                        val sender = context.sender
                                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                        val lineNum = context.arguments.byType<Int>()?.value ?: return@execute
                                        val line = context.arguments.greedyByType<String>()?.value?.joinToString(" ") ?: return@execute
                                        
                                        if (lineNum !in holo.richLines.indices) {
                                            sender.sendTranslated(
                                                "hologram.edit.text.line.failure.bounds",
                                                "${Colors.errorHighlight.asRichString()}$lineNum</color>",
                                                holo.richLines.size - 1
                                            ) {
                                                "${Colors.error.asRichString()}$it"
                                            }
                                            return@execute
                                        }

                                        holo.richLines[lineNum] = line
                                        Configuration.updateHolo(holo)

                                        return@execute sender.sendTranslated("hologram.edit.text.line.edit",
                                            "${Colors.highlight.asRichString()}${holo.id}</color>",
                                            "${Colors.highlight.asRichString()}${lineNum}</color>"
                                        )
                                    }
                                }

                                this.fail { it.sendInvalidSyntax() }
                            }

                            this.fail { it.sendInvalidSyntax() }
                        }
                        
                        this.argument("removeline", LiteralArgumentType.literal().alias("deleteline")) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.suggestions { context ->
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value
                                        ?: return@suggestions emptySet()
                                    val lines = holo.richLines.size

                                    return@suggestions (0 ..< lines).map { it.toString() }.toSet()
                                }
                                
                                this.execute { context ->
                                    val sender = context.sender
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                    val lineNum = context.arguments.byType<Int>()?.value ?: return@execute

                                    if (lineNum !in holo.richLines.indices) {
                                        sender.sendTranslated(
                                            "hologram.edit.text.line.failure.bounds",
                                            "${Colors.errorHighlight.asRichString()}$lineNum</color>",
                                            holo.richLines.size - 1
                                        ) {
                                            "${Colors.error.asRichString()}$it"
                                        }
                                        return@execute
                                    }
                                    
                                    holo.richLines.removeAt(lineNum)
                                    Configuration.updateHolo(holo)

                                    return@execute sender.sendTranslated("hologram.edit.text.line.remove",
                                        "${Colors.highlight.asRichString()}${holo.id}</color>",
                                        "${Colors.highlight.asRichString()}${lineNum}</color>"
                                    )
                                }
                            }

                            this.fail { it.sendInvalidSyntax() }
                        }
                        
                        this.argument("reset", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                
                                holo.richLines.clear()
                                Configuration.updateHolo(holo)
                                
                                return@execute sender.sendTranslated("hologram.edit.text.line.reset",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>"
                                )
                            }
                        }

                        this.execute { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("billboard", LiteralArgumentType.literal()) {
                        this.argument("billboard", EnumArgumentType.enum(Display.Billboard::class.java)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val billboard = context.arguments.byType<Display.Billboard>()?.value ?: return@execute
                                
                                holo.billboard = billboard
                                Configuration.updateHolo(holo)

                                val insert = Translate.translate("hologram.edit.billboard.${billboard}", sender.locale)
                                return@execute sender.sendTranslated("hologram.edit.billboard",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${insert}</color>"
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("scale", LiteralArgumentType.literal()) {
                        this.argument("scale", FloatArgumentType.range(.01f, 10f)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val scale = context.arguments.byType<Float>()?.value ?: return@execute

                                holo.scale = scale
                                Configuration.updateHolo(holo)

                                return@execute sender.sendTranslated("hologram.edit.scale",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${scale}</color>"
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }

                    this.argument("textAlign", LiteralArgumentType.literal()) {
                        this.argument("textAlignment", EnumArgumentType.enum(TextDisplay.TextAlignment::class.java)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val alignment = context.arguments.byType<TextDisplay.TextAlignment>()?.value ?: return@execute

                                holo.textAlign = alignment
                                Configuration.updateHolo(holo)

                                val insert = Translate.translate("hologram.edit.text_alignment.${alignment}", sender.locale)
                                return@execute sender.sendTranslated("hologram.edit.text_alignment",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${insert}</color>"
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("textShadow", LiteralArgumentType.literal()) {
                        this.argument("textShadow", BooleanArgumentType.bool()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val state = context.arguments.byType<Boolean>()?.value ?: return@execute
                                
                                holo.textShadow = state
                                Configuration.updateHolo(holo)
                                
                                val insert = Translate.translate("hologram.edit.text_shadow.${state}", sender.locale)
                                return@execute sender.sendTranslated("hologram.edit.text_shadow",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${insert}</color>"
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("backgroundColor", LiteralArgumentType.literal()) {
                        this.argument("backgroundColor", ColorArgumentType) {
                            this.suggestions(setOf(
                                "blue", "#fff", "#bb0033", "#660033AA", "-22016"
                            ))
                            
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val color = context.arguments.byType<BukkitColor>()?.value ?: return@execute
                                
                                holo.backgroundColor = color
                                Configuration.updateHolo(holo)
                                
                                return@execute sender.sendTranslated("hologram.edit.background_color",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${TextColor.color(color.red, color.green, color.blue).asRichString()}${color.asRGB()}</color>"
                                )
                            }
                        }
                        
                        this.argument("reset", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                
                                holo.backgroundColor = null
                                Configuration.updateHolo(holo)

                                return@execute sender.sendTranslated("hologram.edit.background_color.reset",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("visibilityRange", LiteralArgumentType.literal()) {
                        this.argument("range", DoubleArgumentType.range(1.0, Double.MAX_VALUE)) {
                            this.suggestions(setOf(
                                "20", "50", "100"
                            ))
                            
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val range = context.arguments.byType<Double>()?.value ?: return@execute
                                
                                holo.visibilityRange = range
                                Configuration.updateHolo(holo)

                                return@execute sender.sendTranslated("hologram.edit.visibility_range",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}~${range}</color>"
                                )
                            }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("visibility", LiteralArgumentType.literal()) {
                        this.argument("state", BooleanArgumentType.bool()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val state = context.arguments.byType<Boolean>()?.value ?: return@execute
                                
                                holo.visible = state
                                Configuration.updateHolo(holo)

                                val insert = Translate.translate("hologram.edit.visibility.${state}", sender.locale)
                                return@execute sender.sendTranslated("hologram.edit.visibility",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${insert}</color>"
                                )
                            }
                        }
                        
                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("attribute", LiteralArgumentType.literal()) {
                        this.argument("attribute", HologramAttributeArgumentType) {
                            this.argument("value", FloatArgumentType.positive()) {
                                this.suggestions { context ->
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@suggestions emptySet()
                                    val attributeClass = context.arguments.byType<Class<out HologramAttribute>>()?.value ?: return@suggestions emptySet()
                                    
                                    holo.getAttribute(attributeClass)?.value?.let { 
                                        return@suggestions setOf(it.toString())
                                    }
                                    
                                    HologramAttribute.DEFAULT_ATTRIBUTES.find { it.javaClass == attributeClass }?.let { 
                                        return@suggestions setOf(it.value.toString())
                                    }
                                    
                                    return@suggestions setOf("1.0")
                                }
                                
                                this.execute { context ->
                                    val sender = context.sender
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                    val attributeClass = context.arguments.byType<Class<out HologramAttribute>>()?.value ?: return@execute
                                    val value = context.arguments.byType<Float>()?.value ?: return@execute
                                    
                                    val constructor = attributeClass.getDeclaredConstructor(Float::class.java)
                                    constructor.isAccessible = true

                                    val instance = constructor.newInstance(value) as HologramAttribute
                                    
                                    holo.setAttribute(instance)
                                    Configuration.updateHolo(holo)

                                    return@execute sender.sendTranslated("hologram.edit.attribute",
                                        "${Colors.highlight.asRichString()}${holo.id}</color>",
                                        "${Colors.highlight.asRichString()}${attributeClass.simpleName}</color>",
                                        "${Colors.highlight.asRichString()}${value}</color>",
                                    )
                                }
                            }
                            this.argument("reset", LiteralArgumentType.literal()) {
                                this.execute { context ->
                                    val sender = context.sender
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                    val attributeClass = context.arguments.byType<Class<out HologramAttribute>>()?.value ?: return@execute
                                    
                                    val default = HologramAttribute.DEFAULT_ATTRIBUTES.find { 
                                        it.javaClass == attributeClass
                                    }
                                    
                                    if (default != null) holo.setAttribute(default)
                                    else holo.removeAttribute(attributeClass)
                                    
                                    Configuration.updateHolo(holo)

                                    return@execute sender.sendTranslated("hologram.edit.attribute.reset",
                                        "${Colors.highlight.asRichString()}${holo.id}</color>",
                                        "${Colors.highlight.asRichString()}${attributeClass.simpleName}</color>"
                                    )
                                }
                            }

                            this.fail { it.sendInvalidSyntax() }
                        }

                        this.fail { it.sendInvalidSyntax() }
                    }
                    
                    this.argument("animation", LiteralArgumentType.literal()) {
                        this.argument("name", HologramAnimationArgumentType) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val animation = context.arguments.byType<HologramAnimation<*>>()?.value ?: return@execute

                                holo.animation = animation
                                Configuration.updateHolo(holo)

                                return@execute sender.sendTranslated("hologram.edit.animation",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>",
                                    "${Colors.highlight.asRichString()}${animation::class.simpleName}</color>"
                                )
                            }
                        }
                        
                        this.argument("none", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute

                                holo.animation = null
                                Configuration.updateHolo(holo)

                                return@execute sender.sendTranslated("hologram.edit.animation.none",
                                    "${Colors.highlight.asRichString()}${holo.id}</color>"
                                )
                            }
                        }
                        
                        this.fail { it.sendInvalidSyntax() }
                    }

                    this.fail { it.sendInvalidSyntax() }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("delete", LiteralArgumentType.literal()) {
                this.permission(Permissions.MODIFY)
                
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender
                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                        
                        Configuration.holograms -= holo
                        holo.remove()

                        sender.sendTranslated("hologram.delete",
                            "${Colors.highlight.asRichString()}${holo.id}</color>"
                        )
                    }
                }

                this.fail { it.sendInvalidSyntax() }
            }
            
            this.argument("debug", LiteralArgumentType.literal()) {
                this.permission("minecraft.command.debug")
                
                this.execute { context ->
                    val sender = context.sender as? Player ?: return@execute
                    sender.isHoloDebug = !sender.isHoloDebug

                    sender.sendTranslated("hologram.debug.${sender.isHoloDebug}")
                }
            }
            
            this.fail { it.sendInvalidSyntax() }
        }
    }
}

private fun CommandContext.sendInvalidSyntax() {
    val sender = this.sender
    sender.sendTranslated("command.invalid-syntax", this.fullCommand) {
        "${Colors.error.asRichString()}$it"
    }
    sender.sendTranslated("command.invalid-syntax.suggest", this.fullCommand)
    sender.playSound(Sound.sound(Key.key("minecraft", "block.anvil.land"), Sound.Source.MASTER, .5f, 1f))
}