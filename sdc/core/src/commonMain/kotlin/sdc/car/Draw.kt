package sdc.car

import math.spatial.div
import math.spatial.minus
import sdc.CanvasContext
import sdc.Car

fun Car.draw(ctx: CanvasContext) {
    ctx.beginPath()
    val halfSize = size / 2
    val rectPos = pos - halfSize
    ctx.rect(rectPos, size)
    ctx.fill()
}