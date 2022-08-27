package ir.syrent.sayanskyblock.storage

import com.moandjiezana.toml.Toml
import ir.sayandevelopment.sayanplaytime.utils.ResourceUtils
import java.io.File


object Settings {

    var configuration: Toml? = null

    var networkName: String? = null
    var host: String? = null
    var port: Int? = null
    var username: String? = null
    var database: String? = null
    var password: String? = null

    init {
        load()
    }

    fun load() {
        var configurationFile = File("configuration.toml")
        if (!configurationFile.exists()) {
            configurationFile = ResourceUtils.copyResource("configuration.toml", configurationFile)
        }
        configuration = Toml().read(configurationFile)


        networkName = configuration?.getString("general.network.name", "Example Network")
        host = configuration?.getString("database.host", "localhost")
        port = configuration?.getLong("database.port", 3306)?.toInt()
        username = configuration?.getString("database.user")
        database = configuration?.getString("database.database")
        password = configuration?.getString("database.password")
    }

}