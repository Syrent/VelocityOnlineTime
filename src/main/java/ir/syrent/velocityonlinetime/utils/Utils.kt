package ir.syrent.velocityonlinetime.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

object Utils {
    fun String.capitalize(): String {
        return this.uppercase()[0].toString() + this.lowercase().substring(1)
    }

    fun Long.format(): String {
        val seconds = this / 1000
        val hours = (seconds / 3600).toInt()
        val minutes = (seconds % 3600 / 60).toInt()
        return "${hours}h ${minutes}m"
    }

    fun String.toComponent(): Component {
        return MiniMessage.miniMessage().deserialize(this)
    }
}