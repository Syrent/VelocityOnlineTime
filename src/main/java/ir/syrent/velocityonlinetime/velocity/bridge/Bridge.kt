package ir.syrent.velocityonlinetime.velocity.bridge

interface Bridge {

    fun sendPluginMessage(sender: Any, messageByte: ByteArray)

    fun sendPluginMessage(messageByte: ByteArray)

}