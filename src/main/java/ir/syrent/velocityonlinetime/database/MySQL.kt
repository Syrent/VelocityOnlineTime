package ir.syrent.velocityonlinetime.database

import java.sql.DriverManager

class MySQL(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val username: String,
    private val password: String
) : SQL() {
    override fun openConnection() {
        Class.forName("com.mysql.cj.jdbc.Driver")
        connection = DriverManager.getConnection("jdbc:mysql://$host:$port/$database", username, password)
    }
}