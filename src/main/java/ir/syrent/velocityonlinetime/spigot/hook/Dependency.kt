package ir.syrent.velocityonlinetime.spigot.hook

import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityonlinetime.spigot.storage.Settings
import ir.syrent.velocityonlinetime.utils.component

abstract class Dependency constructor(val name: String) {

    val exists = Ruom.hasPlugin(name)

    fun register() {
        if (Settings.showDependencySuggestions) {
            sendDescription()
            sendFeatures()
        }
    }

    fun sendDescription() {
        description().map {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} $it".component())
        }
    }

    fun sendFeatures() {
        features().map {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} ${formatFeature(it)}".component())
        }
    }

    open fun features(): List<String> {
       return emptyList()
    }

    open fun description(): List<String> {
        return if (exists) {
            mutableListOf(
                "<green>$name found! dependency hook activated."
            )
        } else {
            mutableListOf(
                "<yellow>You may need to install <green>$name</green> to take full advantage of the plugin features."
            )
        }.apply {
            if (features().isNotEmpty()) this.add("<white>$name advantages are listed below:")
        }
    }

    open fun formatFeature(feature: String): String {
        return "<white>⬤ <gray>$feature"
    }
}