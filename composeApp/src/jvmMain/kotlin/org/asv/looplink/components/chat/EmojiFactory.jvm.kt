package org.asv.looplink.components.chat

import java.awt.Robot
import java.awt.event.KeyEvent

actual fun openEmojiPanel(x: Int, y: Int) {
    val os = System.getProperty("os.name")
    if (os.startsWith("Windows")) {
        try {
            val robot = Robot()

//            if(x >= 0 && y >= 0){
//                robot.mouseMove(x, y)
//            }

//            println("Mouse at: $x, $y")

            robot.keyPress(KeyEvent.VK_WINDOWS)
            robot.keyPress(KeyEvent.VK_PERIOD)

            robot.keyRelease(KeyEvent.VK_PERIOD)
            robot.keyRelease(KeyEvent.VK_WINDOWS)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        println("openEmojiPanel() is only supported on Windows.")
    }
}