package ir.syrent.sayanskyblock.storage

import ir.sayandevelopment.sayanplaytime.storage.Message
import ir.sayandevelopment.sayanplaytime.utils.TextReplacement
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path


object Settings {

    private val messages = mutableMapOf<Message, String>()

    private var settingsLoader = YamlConfigurationLoader.builder().path(Path.of("settings.yml")).build()
    private var databaseLoader = YamlConfigurationLoader.builder().path(Path.of("database.yml")).build()
    private var messagesLoader = YamlConfigurationLoader.builder().path(Path.of("messages.yml")).build()

    var settingsConfig: CommentedConfigurationNode? = null
    var databaseConfig: CommentedConfigurationNode? = null
    var messagesConfig: CommentedConfigurationNode? = null

    var databaseType: String? = "mysql"
    var host: String? = "localhost"
    var port: Int? = 3306
    var username: String? = "root"
    var database: String? = "minecraft"
    var password: String? = ""

    init {
        load()
        refresh()
    }

    private fun load() {
        settingsConfig = settingsLoader.load()
        settingsLoader.save(settingsConfig)
        databaseConfig = databaseLoader.load()
        databaseLoader.save(databaseConfig)
        messagesConfig = messagesLoader.load()
        messagesLoader.save(messagesConfig)

        databaseType = databaseConfig?.node("type")?.string ?: "mysql"
        host = databaseConfig?.node("host")?.string ?: "localhost"
        port = databaseConfig?.node("port")?.int ?: 3306
        username = databaseConfig?.node("username")?.string ?: "root"
        database = databaseConfig?.node("database")?.string ?: "minecraft"
        password = databaseConfig?.node("password")?.string ?: ""

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = messagesConfig?.node(message.path)?.string ?: messagesConfig?.node(Message.UNKNOWN_MESSAGE.path)?.string ?: ""
            }
        }
    }

    fun refresh() {
        settingsLoader.save(settingsConfig)
        databaseLoader.save(databaseConfig)
        messagesLoader.save(messagesConfig)
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        val string = getMessage(message)
        string.replace("\$prefix", getMessage(Message.PREFIX))
        string.replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
        string.replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
        string.replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            string.replace("\$${replacement.from}", replacement.to)
        }
        return string
    }

    fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE] ?: "Unknown message"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }

}