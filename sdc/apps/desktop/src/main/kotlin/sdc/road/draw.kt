package sdc.road

import math.lerp
import sdc.Road
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D

fun Road.draw(g2d: Graphics2D) {
    g2d.color = Color.WHITE
//    g2d.stroke = BasicStroke(lineWidth)

    for (i in 0..laneCount) {
        val x = lerp(left, right, i.toDouble() / laneCount)
        g2d.drawLine(x.toInt(), top.toInt(), x.toInt(), bottom.toInt())
    }
}