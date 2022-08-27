package ir.syrent.sayanskyblock.storage

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.nio.file.Path


object Settings {

    private var settingsLoader: YamlConfigurationLoader? = null

    var settingsConfig: CommentedConfigurationNode? = null

    var networkName: String? = null
    var type: String? = null
    var host: String? = null
    var port: Int? = null
    var username: String? = null
    var database: String? = null
    var password: String? = null

    init {
        load()
        refresh()
    }

    fun load() {
        settingsLoader = yamlConfig("settings")

        settingsConfig = settingsLoader?.load()

        networkName = settingsConfig?.node("general", "network_name")?.getString("ExampleNetwork")
        type = settingsConfig?.node("database", "type")?.getString("mysql")
        host = settingsConfig?.node("database", "host")?.getString("localhost")
        port = settingsConfig?.node("database", "port")?.getInt(3306)
        username = settingsConfig?.node("database", "username")?.getString("root")
        database = settingsConfig?.node("database", "database")?.getString("minecraft")
        password = settingsConfig?.node("database", "password")?.getString("")
    }

    private fun yamlConfig(name: String): YamlConfigurationLoader {
        val file = File("plugins/SayanPlaytime/$name.yml")
        val parent = file.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }
        if (!file.exists()) file.createNewFile()
        return YamlConfigurationLoader.builder().path(Path.of(file.path)).build()
    }

    fun refresh() {
        settingsLoader?.save(settingsConfig)
    }

}