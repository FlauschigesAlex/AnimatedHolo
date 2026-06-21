package at.flauschigesalex.animated_holo.core

import at.flauschigesalex.animated_holo.core.holo.Holograms
import at.flauschigesalex.animated_holo.core.holo.asTextDisplay
import at.flauschigesalex.animated_holo.core.holo.command.HologramAnimationArgumentType
import at.flauschigesalex.animated_holo.core.holo.command.HologramArgumentType
import at.flauschigesalex.animated_holo.core.holo.command.HologramAttributeArgumentType
import at.flauschigesalex.animated_holo.core.holo.isHoloDebug
import at.flauschigesalex.animated_holo.core.holo.remove
import at.flauschigesalex.animated_holo.lib.holo.HologramConfiguration
import at.flauschigesalex.animated_holo.lib.holo.animation.HologramAnimation
import at.flauschigesalex.animated_holo.lib.holo.attributes.HologramAttribute
import at.flauschigesalex.animated_holo.lib.holo.position.toLocation
import at.flauschigesalex.animated_holo.lib.holo.position.toPosition
import at.flauschigesalex.animated_holo.lib.utils.BukkitColor
import at.flauschigesalex.animated_holo.lib.utils.ColorArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.CommandBuilder
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.GreedyArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.internal.LiteralArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.BooleanArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.EnumArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.StringArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.DoubleArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.FloatArgumentType
import at.flauschigesalex.lib.minecraft.brigadier.types.primitive.number.IntegerArgumentType
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

object CommandRegister {
    init {

        CommandBuilder("hologram") {
            this.alias("holo", "aholo", "ahologram")
            
            this.argument("create", LiteralArgumentType.literal()) {
                this.argument("name", StringArgumentType.string()) {
                    
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute // TODO
                        val name = context.arguments.byType<String>()?.value ?: return@execute

                        val holo = HologramConfiguration(name, sender.location.clone().let { 
                            it.yaw = 0f
                            it.pitch = 0f
                            it.toPosition()
                        })
                        Configuration.holograms += holo
                        val entity = holo.asTextDisplay()
                    }
                }
            }
            
            this.argument("near", LiteralArgumentType.literal().alias("list")) {
                this.argument("distance", IntegerArgumentType.positive()) {
                    this.optional()
                    
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute
                        val distance = context.arguments.byType<Int>()?.value ?: 20
                        
                        val holograms = Holograms.toMutableList()
                        holograms.removeIf { it.position.toLocation().distance(sender.location) > distance }
                        
                        sender.sendMessage("Found ${holograms.size} holograms within ${distance} blocks")
                    }
                }
            }
            
            this.argument("move", LiteralArgumentType.literal()) {
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute
                        val hologram = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                    
                        hologram.position = sender.location.toPosition()  
                        Configuration.updateHolo(hologram)
                    }
                }
            }
            
            this.argument("tp", LiteralArgumentType.literal()) {
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender as? Player ?: return@execute
                        val hologram = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                    
                        sender.teleport(hologram.position.toLocation())
                    }
                }
            }
            
            this.argument("edit", LiteralArgumentType.literal()) {
                this.argument("hologram", HologramArgumentType) {
                    this.argument("text", LiteralArgumentType.literal()) {
                        this.argument("addline", LiteralArgumentType.literal()) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.optional()
                                
                                this.argument("text", GreedyArgumentType.greedy(StringArgumentType.string())) {
                                    this.execute { context ->
                                        val sender = context.sender
                                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                        val lineNum = context.arguments.byType<Int>()?.value
                                        val line = context.arguments.greedyByType<String>()?.value?.joinToString(" ") ?: return@execute

                                        if (lineNum == null) {
                                            holo.richLines.addLast(line)
                                            Configuration.updateHolo(holo)
                                            return@execute
                                        }
                                        
                                        holo.richLines.add(lineNum, line)
                                        Configuration.updateHolo(holo)
                                    }
                                }
                            }
                        }
                        
                        this.argument("editline", LiteralArgumentType.literal().alias("setline")) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.argument("text", GreedyArgumentType.greedy(StringArgumentType.string())) {
                                    this.execute { context ->
                                        val sender = context.sender
                                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                        val lineNum = context.arguments.byType<Int>()?.value ?: return@execute
                                        val line = context.arguments.greedyByType<String>()?.value?.joinToString(" ") ?: return@execute
                                        
                                        holo.richLines[lineNum] = line
                                        Configuration.updateHolo(holo)
                                    }
                                }
                            }
                        }
                        
                        this.argument("removeline", LiteralArgumentType.literal().alias("deleteline")) {
                            this.argument("lineNum", IntegerArgumentType.range(0, Int.MAX_VALUE)) {
                                this.execute { context ->
                                    val sender = context.sender
                                    val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                    val lineNum = context.arguments.byType<Int>()?.value ?: return@execute
                                    
                                    holo.richLines.removeAt(lineNum)
                                    Configuration.updateHolo(holo)
                                }
                            }
                        }
                        
                        this.argument("clear", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                
                                holo.richLines.clear()
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                    
                    this.argument("billboard", LiteralArgumentType.literal()) {
                        this.argument("billboard", EnumArgumentType.enum(Display.Billboard::class.java)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val billboard = context.arguments.byType<Display.Billboard>()?.value ?: return@execute
                                
                                holo.billboard = billboard
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                    
                    this.argument("scale", LiteralArgumentType.literal()) {
                        this.argument("scale", FloatArgumentType.range(.01f, 10f)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val scale = context.arguments.byType<Float>()?.value ?: return@execute

                                holo.scale = scale
                                Configuration.updateHolo(holo)
                            }
                        }
                    }

                    this.argument("textAlign", LiteralArgumentType.literal()) {
                        this.argument("textAlignment", EnumArgumentType.enum(TextDisplay.TextAlignment::class.java)) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val alignment = context.arguments.byType<TextDisplay.TextAlignment>()?.value ?: return@execute

                                holo.textAlign = alignment
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                    this.argument("textShadow", LiteralArgumentType.literal()) {
                        this.argument("textShadow", BooleanArgumentType.bool()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val state = context.arguments.byType<Boolean>()?.value ?: return@execute
                                
                                holo.textShadow = state
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                    
                    this.argument("backgroundColor", LiteralArgumentType.literal()) {
                        this.argument("backgroundColor", ColorArgumentType) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val color = context.arguments.byType<BukkitColor>()?.value
                                
                                holo.backgroundColor = color
                                Configuration.updateHolo(holo)
                            }
                        }
                        
                        this.argument("reset", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                
                                holo.backgroundColor = null
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                    
                    this.argument("visibilityRange", LiteralArgumentType.literal()) {
                        this.argument("range", DoubleArgumentType.positive()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val range = context.arguments.byType<Double>()?.value ?: return@execute
                                
                                holo.visibilityRange = range
                                Configuration.updateHolo(holo)
                            }
                        }
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
                                }
                            }
                        }
                    }
                    
                    this.argument("animation", LiteralArgumentType.literal()) {
                        this.argument("attribute", HologramAnimationArgumentType) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                                val animation = context.arguments.byType<HologramAnimation<*>>()?.value ?: return@execute

                                holo.animation = animation
                                Configuration.updateHolo(holo)
                            }
                        }
                        this.argument("none", LiteralArgumentType.literal()) {
                            this.execute { context ->
                                val sender = context.sender
                                val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute

                                holo.animation = null
                                Configuration.updateHolo(holo)
                            }
                        }
                    }
                }
            }
            
            this.argument("delete", LiteralArgumentType.literal()) {
                this.argument("hologram", HologramArgumentType) {
                    this.execute { context ->
                        val sender = context.sender
                        val holo = context.arguments.byType<HologramConfiguration>()?.value ?: return@execute
                        
                        Configuration.holograms -= holo
                        holo.remove()
                    }
                }
            }
            
            this.argument("debug", LiteralArgumentType.literal()) {
                this.execute { context ->
                    val sender = context.sender as? Player ?: return@execute
                    sender.isHoloDebug = !sender.isHoloDebug
                }
            }
        }
    }
}