package ir.syrent.velocityonlinetime.utils

object Utils {
    fun capitalize(string: String): String {
        return string.uppercase()[0].toString() + string.lowercase().substring(1)
    }

    fun formatTime(total_time: Long): String {
        val seconds = total_time / 1000
        val hours = (seconds / 3600).toInt()
        val minutes = (seconds % 3600 / 60).toInt()
        return "${hours}h ${minutes}m"
    }
}