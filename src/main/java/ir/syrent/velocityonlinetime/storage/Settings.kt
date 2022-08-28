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
    var discordToken: String = ""
    var weeklyTopChannel: String = ""
    var staffOnlineTimeChannel: String = ""

    init {
        load()
    }

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
        port = configuration?.getLong("database.port", 3306)?.toInt() ?: 3306
        username = configuration?.getString("database.user")!!
        database = configuration?.getString("database.database")!!
        password = configuration?.getString("database.password")!!

        /* Discord */
        discordToken = configuration?.getString("discord.token")!!
        weeklyTopChannel = configuration?.getString("discord.weekly")!!
        staffOnlineTimeChannel = configuration?.getString("discord.staff")!!
    }

}