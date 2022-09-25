package sdc.car

import math.point.*
import math.spatial.*
import sdc.Car
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

fun Car.draw(g: Graphics2D) {
    // calculate positions
    val p = (pos - (0.5 * size)).map { it.toInt() }

    // draw
//    val g2d = g.create() as Graphics2D
    val g2d = g
    g2d.color = Color.BLACK
    val at = AffineTransform()
    at.rotate(angle, pos.x, pos.y)
    g2d.transform = at
    g2d.fillRect(p.x, p.y, size.x, size.y)
//    g2d.dispose()
    // draw
}