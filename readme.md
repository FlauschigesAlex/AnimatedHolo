# AnimatedHolo
A plugin designed to create, edit and animate TextDisplay-based holograms directly in-game.<br>
It provides configurable hologram text, display settings, hover detection and simple animations while storing all holograms in a readable JSON configuration file.

### Optional Plugins
- [ProtocolLib 5.4.0 (or newer)](https://github.com/dmulloy2/ProtocolLib/) - Required together with PlaceholderAPI for per-player placeholder rendering.
- [PlaceholderAPI 2.12.2 (or newer)](https://www.spigotmc.org/resources/placeholderapi.6245/) - Required together with ProtocolLib for per-player placeholder rendering.

### Supported platforms and versions
- [Paper](https://papermc.io/software/paper/) Versions: 1.21.10 - 26.2
- [PurpurMC](https://purpurmc.org/) Versions: 1.21.10 - 26.2

Recommended Version: Paper 1.21.11 (or newer)
<br>Although AnimatedHolo may work on other platforms or versions, I do not guarantee their stability or functionality.

## Setup
### Creating holograms
`/hologram create <name>` - Creates a new hologram at your current location.
<br>`/hologram near [distance]` - Lists the amount of holograms within the given distance. Defaults to `20`.
<br>`/hologram move <hologram>` - Moves an existing hologram to your current location.
<br>`/hologram tp <hologram>` - Teleports you to an existing hologram.
<br>`/hologram delete <hologram>` - Deletes an existing hologram.

Aliases: `/holo`, `/aholo`, `/ahologram`

### Text setup
Text supports [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/).
<br>If PlaceholderAPI and ProtocolLib are installed, placeholders are rendered per player.

`/hologram edit <hologram> text addline [lineNum] <text>` - Adds a new line to the hologram.
<br>`/hologram edit <hologram> text editline <lineNum> <text>` - Replaces a line.
<br>`/hologram edit <hologram> text setline <lineNum> <text>` - Alias for `editline`.
<br>`/hologram edit <hologram> text removeline <lineNum>` - Removes a line.
<br>`/hologram edit <hologram> text deleteline <lineNum>` - Alias for `removeline`.
<br>`/hologram edit <hologram> text clear` - Removes all lines.

### Display setup
`/hologram edit <hologram> billboard <billboard>` - Sets the TextDisplay billboard mode.
<br>`/hologram edit <hologram> scale <scale>` - Sets the hologram scale. Limited to `0.01` - `10`.
<br>`/hologram edit <hologram> textAlign <textAlignment>` - Sets the text alignment.
<br>`/hologram edit <hologram> textShadow <true|false>` - Toggles text shadow.
<br>`/hologram edit <hologram> backgroundColor <color>` - Sets the background color.
<br>`/hologram edit <hologram> backgroundColor reset` - Resets the background color.
<br>`/hologram edit <hologram> visibilityRange <range>` - Sets the visibility range.

### Animations
`/hologram edit <hologram> animation <animation>` - Sets the hologram animation.
<br>`/hologram edit <hologram> animation none` - Disables the hologram animation.

Available by default:
- `BobbingAnimation` - Moves the hologram up and down.

### Attributes
Attributes modify hover detection, hover scaling and animation behaviour.

`/hologram edit <hologram> attribute <attribute> <value>` - Sets an attribute value.
<br>`/hologram edit <hologram> attribute <attribute> reset` - Resets an attribute to its default value or removes it if no default exists.

Available attributes:
- `HoverScaleAttribute` - Controls the scale while the hologram is hovered. Defaults to `1.5`.
- `HoverTransitionAttribute` - Controls the hover transition speed. Defaults to `3`.
- `HoverOffsetAttribute` - Offsets the raytrace interaction point.
- `HoverMaxInteractionRangeAttribute` - Limits how far away the hologram can be interacted with.
- `HoverInteractionRangeMultiplierAttribute` - Multiplies the hover interaction range.
- `AnimationModifierAttribute` - Modifies the active animation strength.

### Debug
`/hologram debug` - Toggles hologram raytrace debug mode for yourself.

## Configuration file
AnimatedHolo uses a configuration file to store holograms and global options.
<br>**If you want to make any changes to the config manually, edit `plugins/AnimatedHolo/config.json` while the server is stopped.**

### REMOVE COMMENTS BEFORE APPLYING CONFIG
```json
{
  "_version": 1,
  "animations": {
    "enabled": true
  },
  "holograms": [
    {
      "id": "example",
      "position": {
        "world": "world",
        "x": 0.0,
        "y": 64.0,
        "z": 0.0,
        "yaw": 0.0,
        "pitch": 0.0
      },
      "richLines": [
        "<rainbow>Example Hologram"
      ],
      "scale": 1.0,
      "visibilityRange": 50.0,
      "visible": true,
      "billboard": "CENTER",
      "backgroundColor": null,
      "textAlign": "CENTER",
      "textShadow": true
    }
  ]
}
```
### REMOVE COMMENTS BEFORE APPLYING CONFIG

## Developer API
AnimatedHolo includes a library module for creating and modifying holograms from other plugins.

### Maven
```xml
<repository>
    <id>animated-holo</id>
    <url>https://repo.flauschigesalex.at/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>at.flauschigesalex.animated-holo</groupId>
    <artifactId>animated-holo-lib</artifactId>
    <version>VERSION</version>
</dependency>
```
