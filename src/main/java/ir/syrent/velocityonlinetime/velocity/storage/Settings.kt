package ir.syrent.velocityonlinetime.velocity.storage

import ir.syrent.velocityonlinetime.spigot.utils.getUsername
import ir.syrent.velocityonlinetime.utils.TextReplacement
import me.mohamad82.ruom.configuration.ConfigurateYamlConfig
import org.spongepowered.configurate.CommentedConfigurationNode


object Settings {

    private val messages = mutableMapOf<Message, String>()

    /* General */
    lateinit var defaultLanguage: String

    init {
        load()
    }

    fun load() {
        val settingsYaml = ConfigurateYamlConfig("settings.yml")
        settingsYaml.create()
        settingsYaml.load()
        val settingsRoot = settingsYaml.root!!

        defaultLanguage = settingsRoot.node("default_language").string ?: "en_US"

        val languageYaml = ConfigurateYamlConfig("languages/${defaultLanguage}.yml")
        languageYaml.create()
        languageYaml.load()
        val languageRoot = languageYaml.root!!

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                var section: CommentedConfigurationNode? = null
                for (part in message.path.split(".")) {
                    section = if (section == null) languageRoot.node(part) else section.node(part)
                }

                this[message] = section?.string ?: "Cannot find message: ${message.name} in ${message.path}"
            }
        }

        settingsYaml.load()
        languageYaml.load()
    }

    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", if (listOf("player", "name", "username").contains(replacement.from)) replacement.from.getUsername() else replacement.to)
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
        return messages[message] ?: "Unknown message ($message)"
    }

}