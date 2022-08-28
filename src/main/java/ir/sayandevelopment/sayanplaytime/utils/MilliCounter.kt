package ir.sayandevelopment.sayanplaytime.utils

import java.text.DecimalFormat
import kotlin.math.pow

class MilliCounter {

    private var decimalFormat = DecimalFormat("#.00")
    private var time: Long = 0
    private var elapsed = 0.0

    fun start() {
        time = System.nanoTime()
    }

    fun stop() {
        elapsed = (System.nanoTime() - time) * 10.0.pow(-6.0)
    }

    fun get(): Float {
        return decimalFormat.format(elapsed).toFloat()
    }
}