package ir.syrent.velocityonlinetime.storage

import com.moandjiezana.toml.Toml
import ir.syrent.velocityonlinetime.utils.ResourceUtils
import ir.syrent.velocityonlinetime.utils.TextReplacement
import java.io.File


object Settings {

    private lateinit var configurationToml: Toml
    private lateinit var messagesToml: Toml

    private val messages = mutableMapOf<Message, String>()

    /* General */
    lateinit var networkName: String

    /* Database */
    lateinit var host: String
    lateinit var username: String
    lateinit var database: String
    lateinit var password: String
    var port: Int = 3306

    /* Discord */
    lateinit var weeklyTopRewards: List<String>
    lateinit var weeklyServerAnnouncementContent: List<String>
    lateinit var discordToken: String
    var weeklyEnabled: Boolean = false
    var weeklyDayOfWeek: Int = 7
    var weeklyHourOfDay: Int = 0
    var weeklyTopGiveReward: Boolean = false
    var weeklyServerAnnouncementEnabled: Boolean = false
    var staffOnlineTimeEnabled: Boolean = false
    var discordDailyAppendGamemodes: Boolean = false
    var weeklyChannelID: Long = 0
    var dailyChannelID: Long = 0

    /* Messages */
    lateinit var rawPrefix: String
    lateinit var prefix: String
    lateinit var onlineTime: String
    lateinit var discordWeeklyTitle: String
    lateinit var discordWeeklyURL: String
    lateinit var discordWeeklyColor: String
    lateinit var discordWeeklyDescription: List<String>
    lateinit var discordWeeklyFooter: String
    lateinit var discordWeeklyThumbnail: String
    lateinit var discordWeeklyContent: List<String>
    lateinit var discordDailyTitle: String
    lateinit var discordDailyURL: String
    lateinit var discordDailyColor: String
    lateinit var discordDailyDescription: List<String>
    lateinit var discordDailyFooter: String
    lateinit var discordDailyThumbnail: String
    lateinit var discordDailyContent: List<String>

    fun load() {
        initializeToml()

        /* Discord */
        weeklyEnabled = getBoolean(configurationToml, "discord.weekly.enabled")
        weeklyDayOfWeek = getInt(configurationToml, "discord.weekly.day_of_week")
        weeklyHourOfDay = getInt(configurationToml, "discord.weekly.hour_of_day")
        weeklyTopGiveReward = getBoolean(configurationToml, "discord.weekly.give_reward")
        weeklyTopRewards = getStringList(configurationToml, "discord.weekly.rewards")
        weeklyServerAnnouncementEnabled = getBoolean(configurationToml, "discord.weekly.send_server_message.enabled")
        weeklyServerAnnouncementContent = getStringList(configurationToml, "discord.weekly.server_announcement.content")
        staffOnlineTimeEnabled = getBoolean(configurationToml, "discord.staff.enabled")
        discordToken = getString(configurationToml, "discord.token")
        weeklyChannelID = getLong(configurationToml, "discord.weekly.channel_id")
        dailyChannelID = getLong(configurationToml, "discord.daily.channel_id")
        discordDailyAppendGamemodes = getBoolean(configurationToml, "discord.daily.append_gamemodes")

        /* Messages */
        rawPrefix = getString(messagesToml, "general.prefix")
        prefix = getString(messagesToml, "general.prefix")
        onlineTime = getString(messagesToml, "global.onlinetime")
        discordWeeklyTitle = getString(messagesToml, "discord.weekly.title")
        discordWeeklyURL = getString(messagesToml, "discord.weekly.url")
        discordWeeklyColor = getString(messagesToml, "discord.weekly.color")
        discordWeeklyDescription = getStringList(messagesToml, "discord.weekly.description")
        discordWeeklyFooter = getString(messagesToml, "discord.weekly.footer")
        discordWeeklyThumbnail = getString(messagesToml, "discord.weekly.thumbnail")
        discordWeeklyContent = getStringList(messagesToml, "discord.weekly.content")
        discordDailyTitle = getString(messagesToml, "discord.daily.title")
        discordDailyURL = getString(messagesToml, "discord.daily.url")
        discordDailyColor = getString(messagesToml, "discord.daily.color")
        discordDailyDescription = getStringList(messagesToml, "discord.daily.description")
        discordDailyFooter = getString(messagesToml, "discord.daily.footer")
        discordDailyThumbnail = getString(messagesToml, "discord.daily.thumbnail")
        discordDailyContent = getStringList(messagesToml, "discord.daily.content")

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = getString(messagesToml, message.path)
            }
        }
    }

    private fun initializeToml() {
        configurationToml = tomlFile("configuration")
        messagesToml = tomlFile("messages")
    }

    private fun tomlFile(name: String): Toml {
        return File("plugins/VelocityOnlineTime/$name.toml").let {
            var file = it
            val parentFile = file.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            if (!file.exists()) {
                file = ResourceUtils.copyResource("$name.toml", file)
            }
            Toml().read(file)
        }
    }

    private fun getString(configuration: Toml, key: String): String {
        val default = key.split(".").last()
        return configuration.getString(key, default) ?: default
    }

    private fun getStringList(configuration: Toml, key: String): List<String> {
        val default = key.split(".").last()
        return configuration.getList(key, listOf(default)) ?: listOf(default)
    }

    private fun getInt(configuration: Toml, key: String): Int {
        return (configuration.getLong(key, 0) ?: 0).toInt()
    }

    private fun getLong(configuration: Toml, key: String): Long {
        return configuration.getLong(key, 0) ?: 0
    }

    private fun getBoolean(configuration: Toml, key: String): Boolean {
        return configuration.getBoolean(key, false) ?: false
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        var formattedMessage = getMessage(message)
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)
        }
        return formattedMessage
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: formatMessage(Message.UNKNOWN_MESSAGE)
    }

}