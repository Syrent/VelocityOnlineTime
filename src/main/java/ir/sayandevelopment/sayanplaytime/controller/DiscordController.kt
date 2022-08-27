package ir.sayandevelopment.sayanplaytime.controller

import ir.sayandevelopment.sayanplaytime.*
import ir.syrent.sayanskyblock.storage.Message
import ir.syrent.sayanskyblock.storage.Settings
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.minimessage.MiniMessage
import java.awt.Color
import java.util.*
import java.util.concurrent.TimeUnit

class DiscordController(
    private val plugin: SayanPlayTime
): ListenerAdapter() {

    var jda: JDA? = null
    private var playtimeChannel: TextChannel? = null
    private var staffPlayTimeChannel: TextChannel? = null
    private var weeklyPlayTimeSent = false
    private var staffPlayTimeSent = false
    private val miniMessage = MiniMessage.miniMessage()

    init {
        connect()
    }

    private fun connect() {
        var connected = false

        plugin.server.scheduler.buildTask(this, object : Runnable {
            override fun run() {
                if (!connected) {
                    plugin.logger.info("DiscordJDA is not connected! trying to connect...")
                    // TODO: Read bot token from config file
                    jda = JDABuilder
                        .createDefault("ODU1NTk2OTYzNDk5MDgxNzI5.YM0yxA.k01a2PBR68T5v5BZ68LY52aO3R8")
                        .build()
                        .awaitReady()
                    plugin.logger.info("DiscordJDA is now connected!")

                    initializePlayTimeChannels()

                    plugin.server.scheduler.buildTask(this) {
                        plugin.logger.info("Registering Discord event listener...")
                        jda?.addEventListener(this) ?: throw NullPointerException("JDA is null!")
                        plugin.logger.info("Discord event listener successfully registered.")
                        checkTime()
                    }.delay(10L, TimeUnit.SECONDS).schedule()
                    connected = true
                }
            }
        }).delay( /* TODO: Read time from yaml file */3L, TimeUnit.SECONDS).schedule()
    }

    private fun initializePlayTimeChannels() {
        // TODO: Read channel id from config file
        playtimeChannel = jda?.getTextChannelById(967374851939663893L) ?: throw NullPointerException("JDA is null!")
        staffPlayTimeChannel = jda?.getTextChannelById(967374872554635275L) ?: throw NullPointerException("JDA is null")
    }

    /**
     * Checks if the time has passed and sends the playtime message if it has.
     */
    private fun checkTime() {
        plugin.server.scheduler.buildTask(this) {
            val hours = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            // TODO: Read time from yaml file
            if (hours == 0) {
                sendWeeklyPlayTime()
                sendStaffPlayTime()
            } else {
                weeklyPlayTimeSent = false
                staffPlayTimeSent = false
            }
        }.repeat(10, TimeUnit.SECONDS).schedule()
    }

    /**
     * Sends the weekly playtime message to the playtime channel and minecraft chat
     * also give the top online player reward.
     */
    private fun sendWeeklyPlayTime() {
        if (!weeklyPlayTimeSent) {
            // TODO: Read time from yaml file
            if (Calendar.getInstance()[Calendar.DAY_OF_WEEK] == 7) {
                val username = plugin.sql.getWeeklyTops(1)[0].userName

                giveReward(username)

                // TODO: Read message from yaml file
                plugin.server.allPlayers.forEach { player ->
                    player.sendMessage(
                        miniMessage.deserialize(
                            "<bold><gradient:#F09D00:#F8BD04><st>                    </st></gradient></bold>" +
                                    " <gradient:#F2E205:#F2A30F>PlayTime</gradient> " +
                                    "<bold><gradient:#F8BD04:#F09D00><st>                    </st></gradient></bold>"
                        )
                    )
                    player.sendMessage(
                        miniMessage.deserialize(
                            String.format(
                                PlayTimeCommand.PREFIX + "<bold><color:#F2E205>%s Be Onvan Top PlayTime Hafte Barande Rank VIP Shod!",
                                username
                            )
                        )
                    )
                }

                sendWinnerMessage()
                plugin.sql.resetWeekly()
                weeklyPlayTimeSent = true
            }
        }
    }

    private fun sendStaffPlayTime() {
        if (!staffPlayTimeSent) {
            sendDailyMessage()
            plugin.sql.resetDaily()
            staffPlayTimeSent = true
        }
    }

    private fun giveReward(username: String) {
        // TODO: Read reward command from yaml file
        plugin.server.commandManager.executeAsync(
            plugin.server.consoleCommandSource,
            String.format("lpv user %s parent addtemp baron 7d", username)
        )
        plugin.logger.info(String.format("%s won weekly reward!", username))
    }

    // TODO: Read embed content from config file
    private fun sendWinnerMessage() {
        val onlinePlayers: List<OnlinePlayer> = plugin.sql.getWeeklyTops(3)
        val totalTime = onlinePlayers[0].time
        val seconds = totalTime / 1000
        val hours = (seconds / 3600).toInt()
        val minutes = (seconds % 3600 / 60).toInt()

        val embed = EmbedBuilder()
        embed.setTitle("\uD83E\uDDED  PlayTime | ${DateUtils.getCurrentShamsidate()}", null)
        embed.setColor(Color(0xc1d6f1))
        embed.appendDescription("⏱️ مسابقه بیشترین پلی تایم این هفته سرور به پایان رسید!")
        embed.appendDescription("\n")
        embed.appendDescription("\n")
        embed.appendDescription("این هفته ${onlinePlayers[0].userName} با $hours ساعت و $minutes دقیقه پلی تایم برنده شد")
        embed.appendDescription("\n")
        embed.appendDescription("\n")
        embed.appendDescription(
                "\uD83C\uDFC6 نفرات برتر این هفته:\n" +
                "\uD83E\uDD47 ${onlinePlayers[0].userName}\n" +
                "\uD83E\uDD48 ${onlinePlayers[1].userName}\n" +
                "\uD83E\uDD49 ${onlinePlayers[2].userName}\n",
        )
        embed.appendDescription("\n")
        embed.appendDescription(
            "\uD83D\uDD39 شما هم با پلی دادن و دریافت رتبه \uD83E\uDD47 " +
                    "در داخل سرور میتوانید در هر هفته برنده رنک **Baron** به مدت یک هفته بشوید !\n" +
                    "\n" +
                    "\uD83D\uDD39 Play.QPixel.IR\n" +
                    "\uD83D\uDFE3 QPixel.IR/Discord\n" +
                    "\uD83C\uDF10 wWw.QPixel.IR"
        )
        embed.setFooter("${Settings.getMessage(Message.NETWORK_NAME)} | PlayTime")

        embed.setThumbnail("http://cravatar.eu/avatar/${onlinePlayers[0].userName}/64.png")
        playtimeChannel?.sendMessageEmbeds(embed.build())
            ?.append("<@&758758796167348285>")
            ?.queue() ?: throw NullPointerException("Can't send embed message to playtime channel")
    }

    // TODO: Read embed content from config file
    private fun sendDailyMessage() {
        val onlinePlayerList: List<OnlinePlayer> = plugin.sql.dailyPlayTimes
        for (onlinePlayer: OnlinePlayer in onlinePlayerList) {
            val totalTime = onlinePlayer.time

            val embed = EmbedBuilder()
            embed.setTitle(
                String.format(
                    "\uD83E\uDDED %s Daily PlayTime | %s",
                    onlinePlayer.userName,
                    DateUtils.getCurrentShamsidate()
                ), null
            )
            embed.setColor(Color(0x2F5FBE))
            embed.appendDescription("Total Time: ${Utils.formatTime(totalTime)}")
            embed.appendDescription("\n")
            embed.appendDescription("\n")

            for (registeredServer in plugin.server.allServers) {
                val serverName = registeredServer.serverInfo.name
                val serverOnlineTime: Long = plugin.sql.getDailyPlayTime(onlinePlayer.uuid, serverName)

                if (serverOnlineTime > 0) {
                    embed.addField("$serverName: ", Utils.formatTime(serverOnlineTime), true)
                }
            }

            embed.setFooter("${Settings.getMessage(Message.NETWORK_NAME)} | PlayTime")
            embed.setThumbnail(String.format("http://cravatar.eu/avatar/%s/64.png", onlinePlayer.userName))

            staffPlayTimeChannel?.sendMessageEmbeds(embed.build())?.queue() ?: throw NullPointerException("Staff playtime channel not found!")
        }
    }
}