package sdc

import math.XY

interface CanvasContext {
    fun beginPath()
    fun rect(pos: XY<Number>, size: XY<Number>)
    fun fill()
}