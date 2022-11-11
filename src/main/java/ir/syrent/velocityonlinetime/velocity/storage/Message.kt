package ir.syrent.velocityonlinetime.velocity.storage

/**
 * This class responsible
 */
enum class Message(val path: String) {
    RAW_PREFIX("general.raw_prefix"),
    PREFIX("general.prefix"),
    CONSOLE_PREFIX("general.console_prefix"),
    SUCCESSFUL_PREFIX("general.successful_prefix"),
    WARN_PREFIX("general.warn_prefix"),
    ERROR_PREFIX("general.error_prefix"),
    ONLY_PLAYERS("general.only_players"),
    VALID_PARAMS("general.valid_parameters"),
    NO_PERMISSION("command.no_permission"),
    PLAYER_NOT_FOUND("command.player_not_found"),
    PLAYER_NOT_FOUND_SERVER("command.player_not_found_server"),
    ONLINETIME_USAGE("command.onlinetime.usage"),
    ONLINETIME_USE("command.onlinetime.use"),
    ONLINETIME_SERVER_USE("command.onlinetime.server.use"),
    ONLINETIME_WEEK_USE("command.onlinetime.use"),
    ONLINETIME_GET_USE("command.onlinetime.get.use"),
    ONLINETIME_GET_SERVER_USE("command.onlinetime.get.server.use"),
    ONLINETIME_TOP_USE("command.onlinetime.top.week.use"),
    ONLINETIME_TOP_WEEK_USE("command.onlinetime.top.week.use"),
    EMPTY("");
}