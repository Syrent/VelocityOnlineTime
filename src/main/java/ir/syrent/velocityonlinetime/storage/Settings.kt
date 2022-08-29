package ir.syrent.velocityonlinetime.storage

import com.moandjiezana.toml.Toml
import ir.syrent.velocityonlinetime.utils.ResourceUtils
import java.io.File


object Settings {

    private var configuration: Toml? = null

    /* General */
    var networkName: String = "Example Network"

    /* Database */
    var host: String = "localhost"
    var port: Int = 3306
    var username: String = "root"
    var database: String = ""
    var password: String = ""

    /* Discord */
    var weeklyTopEnabled: Boolean = false
    var weeklyTopGiveReward: Boolean = false
    var weeklyTopRewards: List<String> = listOf()
    var weeklyServerAnnouncementEnabled: Boolean = false
    var weeklyServerAnnouncementContent: List<String> = listOf()
    var staffOnlineTimeEnabled: Boolean = false
    var discordToken: String = ""
    var weeklyTopChannel: Long = 0
    var staffOnlineTimeChannel: Long = 0

    /* Messages */
    var prefix: String = "OnlineTime » "

    fun load() {
        var configurationFile = File("plugins/VelocityOnlineTime/configuration.toml")
        val parentFile = configurationFile.parentFile
        if (!parentFile.exists()) {
            parentFile.mkdirs()
        }
        if (!configurationFile.exists()) {
            configurationFile = ResourceUtils.copyResource("configuration.toml", configurationFile)
        }
        configuration = Toml().read(configurationFile)


        /* General */
        networkName = configuration?.getString("general.network.name", "Example Network")!!

        /* Database */
        host = configuration?.getString("database.host", "localhost")!!
        port = configuration?.getLong("database.port", 3306)?.toInt()!!
        username = configuration?.getString("database.user")!!
        database = configuration?.getString("database.database")!!
        password = configuration?.getString("database.password")!!

        /* Discord */
        weeklyTopEnabled = configuration?.getBoolean("discord.weekly.enabled", false)!!
        weeklyTopGiveReward = configuration?.getBoolean("discord.weekly.give_reward", false)!!
        weeklyTopRewards = configuration?.getList("discord.weekly.rewards", listOf())!!
        weeklyServerAnnouncementEnabled = configuration?.getBoolean("discord.weekly.server_announcement.enabled", false)!!
        weeklyServerAnnouncementContent = configuration?.getList("discord.weekly.server_announcement.content", listOf())!!
        staffOnlineTimeEnabled = configuration?.getBoolean("discord.staff.enabled", false)!!
        discordToken = configuration?.getString("discord.token")!!
        weeklyTopChannel = configuration?.getLong("discord.weekly.channel_id")!!
        staffOnlineTimeChannel = configuration?.getLong("discord.staff.channel_id")!!

        /* Messages */
        prefix = configuration?.getString("messages.prefix")!!
    }

}