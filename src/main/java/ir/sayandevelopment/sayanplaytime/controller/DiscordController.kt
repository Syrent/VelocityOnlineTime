package ir.sayandevelopment.sayanplaytime.controller

import ir.sayandevelopment.sayanplaytime.DiscordManager
import ir.sayandevelopment.sayanplaytime.PlayTimeCommand
import ir.sayandevelopment.sayanplaytime.SayanPlayTime
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException

// TODO: Refactor entire class
class DiscordController(
    private val plugin: SayanPlayTime
) {
    var JDA: JDA? = null

    init {
        connect()
    }

    fun connect() {
        var connected = false

        plugin.server.scheduler.buildTask(this, object : Runnable {
            override fun run() {
                if (!connected) {
                    plugin.logger.info("DiscordJDA is not connected! trying to connect...")
                    try {
                        JDA = JDABuilder.createDefault("ODU1NTk2OTYzNDk5MDgxNzI5.YM0yxA.k01a2PBR68T5v5BZ68LY52aO3R8")
                            .build().awaitReady()
                        DiscordManager()
                        plugin.logger.info("DiscordJDA is now connected!")
                        plugin.server.scheduler
                            .buildTask(this) {
                                plugin.logger.info("Registering Discord event listener...")
                                JDA!!.addEventListener(DiscordManager())
                                plugin.logger.info("Discord event listener successfully registered.")
                                checkTime()
                            }
                            .delay(10L, TimeUnit.SECONDS)
                            .schedule()
                        connected = true
                    } catch (e: LoginException) {
                        e.printStackTrace()
                        connected = false
                        plugin.logger.info("Can't connect to Discord.")
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        connected = false
                        plugin.logger.info("Can't connect to Discord.")
                    }
                }
            }
        }).delay( /* TODO: Read time from yaml file */3L, TimeUnit.SECONDS).schedule()
    }


    private fun checkTime() {
        var done = false
        var staffDone = false
        val formatter = MiniMessage.miniMessage()

        plugin.server.scheduler.buildTask(this) {
            val day = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
            val hours = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            if (hours == 0) {
                if (!staffDone) {
                    try {
                        DiscordManager.getInstance().sendDailyMessage()
                        try {
                            plugin.sql.resetDaily()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        staffDone = true
                    } catch (ignored: Exception) {
                    }
                }
                if (!done) {
                    if (day == 7) {
                        try {
                            val username = plugin.sql.getWeeklyTops(1)[0].userName
                            plugin.server.getCommandManager().executeAsync(
                                plugin.server.getConsoleCommandSource(),
                                String.format("lpv user %s parent addtemp baron 7d", username)
                            )
                            plugin.logger.info(String.format("%s won weekly rank!", username))
                            plugin.server.allPlayers.forEach { player ->
                                player.sendMessage(
                                    formatter.deserialize(
                                        "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                                " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                                                "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                                    )
                                )
                                player.sendMessage(
                                    formatter.deserialize(
                                        String.format(
                                            PlayTimeCommand.PREFIX + "<bold><color:#F2E205>%s Be Onvan Top PlayTime Hafte Barande Rank VIP Shod!",
                                            username
                                        )
                                    )
                                )
                            }
                            DiscordManager.getInstance().sendWinnerMessage()
                            try {
                                plugin.sql.resetWeekly()
                                plugin.sql.resetWeekly()
                                plugin.sql.resetWeekly()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            done = true
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
            if (hours == 12) {
                staffDone = false
                done = false
            }
        }.repeat(10, TimeUnit.SECONDS).schedule()
    }
}