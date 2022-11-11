package ir.syrent.velocityonlinetime.spigot.utils

import ir.syrent.velocityonlinetime.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityonlinetime.spigot.storage.Message
import ir.syrent.velocityonlinetime.spigot.storage.Settings
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendMessage(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().sender(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    AdventureApi.get().player(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}
