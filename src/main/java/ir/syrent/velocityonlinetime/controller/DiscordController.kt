package ir.syrent.velocityonlinetime.controller

import ir.syrent.velocityonlinetime.OnlinePlayer
import ir.syrent.velocityonlinetime.VelocityOnlineTime
import ir.syrent.velocityonlinetime.storage.Database
import ir.syrent.velocityonlinetime.storage.Settings
import ir.syrent.velocityonlinetime.utils.Utils.format
import me.mohamad82.ruom.VRuom
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.kyori.adventure.text.minimessage.MiniMessage
import java.awt.Color
import java.util.*
import java.util.concurrent.TimeUnit

class DiscordController(
    private val plugin: VelocityOnlineTime
): EventListener {

    private lateinit var jda: JDA
    private lateinit var weeklyTopChannel: TextChannel
    private lateinit var staffOnlineTimeChannel: TextChannel
    private var weeklyOnlineTimeSent = false
    private var staffOnlineTimeSent = false
    private val miniMessage = MiniMessage.miniMessage()

    init {
        connect()
    }

    private fun connect() {
        VRuom.log("Connecting to Discord bot...")
        jda = JDABuilder.createDefault(Settings.discordToken).addEventListeners(this).build().awaitReady()
        VRuom.log("Connected to Discord bot!")
    }

    private fun shutdown() {
        jda.shutdown()
    }

    override fun onEvent(event: GenericEvent) {
        if (event is ReadyEvent) {
            VRuom.log("DiscordJDA API is ready!")
            initializeOnlineTimeChannels()
            checkTime()
        }
    }

    private fun initializeOnlineTimeChannels() {
        weeklyTopChannel = jda.getTextChannelById(Settings.weeklyChannelID) ?: throw NullPointerException("JDA is null!")
        staffOnlineTimeChannel = jda.getTextChannelById(Settings.dailyChannelID) ?: throw NullPointerException("JDA is null")
    }

    fun sendMessage(embed: MessageEmbed, channel: TextChannel) {
        channel.sendMessageEmbeds(embed).queue()
    }

    /**
     * Checks if the time has passed and sends the onlinetime message if it has.
     */
    private fun checkTime() {
        VRuom.getServer().scheduler.buildTask(plugin) {
            val hours = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            if (hours == Settings.weeklyHourOfDay) {
                if (Settings.weeklyEnabled) {
                    sendWeeklyOnlineTime()
                }
                if (Settings.staffOnlineTimeEnabled) {
                    sendStaffOnlineTime()
                }
            } else {
                weeklyOnlineTimeSent = false
                staffOnlineTimeSent = false
            }
        }.repeat(10, TimeUnit.SECONDS).schedule()
    }

    /**
     * Sends the weekly onlinetime message to the onlinetime channel and minecraft chat
     * also give the top online player reward.
     */
    private fun sendWeeklyOnlineTime() {
        if (!weeklyOnlineTimeSent) {
            if (Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Settings.weeklyDayOfWeek) {
                Database.getWeeklyTop().whenComplete { onlinePlayer, _ ->
                    val username = onlinePlayer.userName

                    if (Settings.weeklyTopGiveReward) {
                        giveReward(username)
                    }

                    if (Settings.weeklyServerAnnouncementEnabled) {
                        VRuom.getServer().allPlayers.forEach { player ->
                            for (line in Settings.weeklyServerAnnouncementContent) {
                                player.sendMessage(miniMessage.deserialize(
                                    line
                                        .replace("\$prefix", Settings.prefix)
                                        .replace("\$player", username)
                                ))
                            }
                        }
                    }

                    sendWinnerMessage()
                    Database.resetWeekly()
                    weeklyOnlineTimeSent = true
                }


            }
        }
    }

    private fun sendStaffOnlineTime() {
        if (!staffOnlineTimeSent) {
            sendDailyMessage()
            Database.resetDaily()
            staffOnlineTimeSent = true
        }
    }

    private fun giveReward(username: String) {
        for (reward in Settings.weeklyTopRewards) {
            VRuom.getServer().commandManager.executeAsync(VRuom.getServer().consoleCommandSource, reward.replace("\$username", username))
        }

        VRuom.log("$username won weekly reward!")
    }

    private fun sendWinnerMessage() {
        Database.getWeeklyTop().whenComplete { onlinePlayer, _ ->
            val time = onlinePlayer.time

            val embed = EmbedBuilder()
            embed.setTitle(Settings.discordWeeklyTitle.replace("\$network", Settings.networkName), Settings.discordWeeklyURL)
            embed.setColor(Color.getColor(Settings.discordWeeklyColor))
            for (line in Settings.discordWeeklyDescription) {
                embed.appendDescription(line.replace("\$time", time.format()))
            }
            embed.setFooter(Settings.discordWeeklyFooter.replace("\$network", Settings.networkName), Settings.discordWeeklyURL)
            embed.setThumbnail(Settings.discordWeeklyThumbnail.replace("\$winner", onlinePlayer.userName))

            weeklyTopChannel.sendMessageEmbeds(embed.build()).apply {
                for (content in Settings.discordWeeklyContent) {
                    this.addContent(content)
                }
            }.queue()
        }
    }

    fun sendDailyMessage() {
        Database.dailyOnlineTimes.whenComplete { onlinePlayers, _ ->
            for (onlinePlayer: OnlinePlayer in onlinePlayers) {
                val totalTime = onlinePlayer.time

                val embed = EmbedBuilder()
                embed.setTitle(Settings.discordDailyTitle.replace("\$network", Settings.networkName), Settings.discordDailyURL)
                embed.setColor(Color.getColor(Settings.discordDailyColor))
                for (line in Settings.discordDailyDescription) {
                    embed.appendDescription(line.replace("\$time", totalTime.format()))
                }

                if (Settings.discordDailyAppendGamemodes) {
                    for (registeredServer in VRuom.getServer().allServers) {
                        val serverName = registeredServer.serverInfo.name
                        Database.getDailyOnlineTime(onlinePlayer.uuid, serverName).whenComplete { time, _ ->
                            if (time > Settings.weeklyHourOfDay) {
                                embed.addField("$serverName: ", time.format(), true)
                            }
                        }
                    }
                }

                embed.setFooter(Settings.discordDailyFooter.replace("\$network", Settings.networkName), Settings.discordDailyURL)
                embed.setThumbnail(Settings.discordDailyThumbnail.replace("\$player", onlinePlayer.userName))

                staffOnlineTimeChannel.sendMessageEmbeds(embed.build()).queue()
            }
        }
    }
}