package ir.sayandevelopment.sayanplaytime.storage

enum class Message(val path: String) {
    NETWORK_NAME("general.network_name"),
    PREFIX("general.prefix"),
    CONSOLE_PREFIX("general.console_prefix"),
    SUCCESSFUL_PREFIX("general.successful_prefix"),
    WARN_PREFIX("general.warn_prefix"),
    ERROR_PREFIX("general.error_prefix"),
    ONLY_PLAYERS("general.only_players"),
    VALID_PARAMS("general.valid_parameters"),
    UNKNOWN_MESSAGE("general.unknown_message"),
    EMPTY("");
}