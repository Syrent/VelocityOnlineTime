package ir.sayandevelopment.sayanplaytime.database

import ir.sayandevelopment.sayanplaytime.SayanPlayTime
import java.sql.DriverManager

class MySQL(
    plugin: SayanPlayTime?,
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) : SQL(plugin!!) {
    override fun openConnection() {
        Class.forName("com.mysql.cj.jdbc.Driver")
        connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database", username, password)
    }
}