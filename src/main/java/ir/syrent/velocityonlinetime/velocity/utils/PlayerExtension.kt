package ir.syrent.velocityonlinetime.velocity.utils

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityonlinetime.utils.TextReplacement
import ir.syrent.velocityonlinetime.utils.component
import ir.syrent.velocityonlinetime.velocity.storage.Message
import ir.syrent.velocityonlinetime.velocity.storage.Settings
import net.kyori.adventure.audience.Audience

fun CommandSource.sendMessage(message: Message, vararg replacements: TextReplacement) {
    Audience.audience(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}

fun Player.sendMessage(message: Message, vararg replacements: TextReplacement) {
    Audience.audience(this).sendMessage(Settings.formatMessage(message, *replacements).component())
}
