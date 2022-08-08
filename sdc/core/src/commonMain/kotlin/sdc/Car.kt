package sdc

import math.MutablePoint2
import math.XY
import math.point.minusAssign
import math.spatial.cordString
import kotlin.math.abs


class Car(
    val pos: MutablePoint2<Double>,
    val size: XY<Int>
) {

    val controls = Controls()

    companion object {
        val MAX_FORWARD_SPEED = 3.0
        val MAX_REVERSE_SPEED = -1.0
        val friction = 0.05
    }

    var speed = 0.0
    var acceleration = 0.2
    fun update() {
        move()
    }

    fun move() {
        if (controls.forward) {
            speed += acceleration
        }

        if (controls.reverse) {
            speed -= acceleration
        }

        if (speed > MAX_FORWARD_SPEED) {
            speed = MAX_FORWARD_SPEED
        }

        if (speed < MAX_REVERSE_SPEED) {
            speed = MAX_REVERSE_SPEED
        }

        if (speed > 0) {
            speed -= friction
        }

        if (speed < 0) {
            speed += friction
        }

        if (abs(speed) < friction) {
            speed = 0.0
        }
        pos.y -= speed
    }

    override fun toString() = "Car(pos=${pos.cordString()},size=${size.cordString()}"
}