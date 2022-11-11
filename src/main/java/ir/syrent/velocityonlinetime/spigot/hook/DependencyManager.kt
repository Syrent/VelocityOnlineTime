package ir.syrent.velocityonlinetime.spigot.hook

import ir.syrent.velocityonlinetime.spigot.VelocityOnlineTimeSpigot

object DependencyManager {

    var placeholderAPIHook: PlaceholderAPIHook
        private set

    init {
        PlaceholderAPIHook(VelocityOnlineTimeSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }

}