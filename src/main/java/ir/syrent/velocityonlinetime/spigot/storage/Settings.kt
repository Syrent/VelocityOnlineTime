package ir.syrent.velocityonlinetime.spigot.storage

import ir.syrent.velocityonlinetime.spigot.configuration.YamlConfig
import ir.syrent.velocityonlinetime.spigot.ruom.Ruom
import ir.syrent.velocityonlinetime.utils.TextReplacement
import org.bukkit.configuration.file.FileConfiguration


object Settings {

    lateinit var settings: YamlConfig
    lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()

    /* General */
    lateinit var defaultLanguage: String
    var velocitySupport = false
    var bstats = true
    var showDependencySuggestions = true

    init {
        load()
    }

    fun load() {
        settings = YamlConfig(Ruom.getPlugin().dataFolder, "settings.yml")
        settingsConfig = settings.config

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"
        velocitySupport = settingsConfig.getBoolean("velocity_support")
        showDependencySuggestions = settingsConfig.getBoolean("show_dependency_suggestions")
        bstats = settingsConfig.getBoolean("bstats")

        language = YamlConfig(Ruom.getPlugin().dataFolder, "languages/$defaultLanguage.yml")
        languageConfig = language.config

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path) ?: "Cannot find message: ${message.name}"
            }
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }

    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)
        }
        return formattedMessage
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
    }

    fun formatMessage(messages: List<String>, vararg replacements: TextReplacement): List<String> {
        val messageList = mutableListOf<String>()
        for (message in messages) {
            messageList.add(formatMessage(message, *replacements))
        }

        return messageList
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE]?.replace(
            "\$error_prefix",
            messages[Message.ERROR_PREFIX] ?: ""
        ) ?: "Unknown message ($message)"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }

}