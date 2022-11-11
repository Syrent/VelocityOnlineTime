package ir.syrent.velocityonlinetime.spigot.utils

import ir.syrent.velocityonlinetime.spigot.ruom.Ruom

fun String.getUsername(): String {
    return Ruom.getOnlinePlayers().find { it.name == this }?.name ?: this
}