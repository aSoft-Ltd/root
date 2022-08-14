package sdc

import math.MutablePoint2
import math.XY
import math.spatial.*
import sdc.car.draw
import sdc.road.draw
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.KeyListener
import java.util.concurrent.Executors
import javax.swing.JFrame
import javax.swing.Timer

class SDC {
    companion object {
        const val FPS = 60

        private val screen = run {
            val s = Toolkit.getDefaultToolkit().screenSize
            XY(s.width, s.height)
        }

        fun <N : Number> XY<N>.toDimension() = Dimension(x.toInt(), y.toInt())

        fun <N : Number> XY<N>.toPoint() = Point(x.toInt(), y.toInt())
    }

    val canvasSize = (screen * XY(0.3, 1.0)).map { it.toInt() }

    private val canvas = Canvas().apply {
        val pos = (screen * XY(0.2, 0.0)).map { it.toInt() }
        size = canvasSize.toDimension()
        bounds = Rectangle(pos.x, pos.y, canvasSize.x, canvasSize.y)
        location = (screen * XY(0.2, 0.0)).toPoint()
        background = Color.LIGHT_GRAY
    }

    private val frame = JFrame("Self Driving Car").apply {
        size = Dimension(Toolkit.getDefaultToolkit().screenSize)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isResizable = true
        setLocationRelativeTo(null)
        add(canvas)
        layout = null
        background = Color.DARK_GRAY
        isVisible = true
    }

    var running = false

    val road = Road(
        centerX = canvasSize.x.toDouble() / 2,
        width = canvasSize.x * 0.9,
        laneCount = 3
    )
    val car = Car(
        pos = MutablePoint2(x = road.getCenterLane(2), 100.0),
        size = XY(30, 50)
    )

    val drawThread = Executors.newSingleThreadExecutor()

    val updateThread = Executors.newSingleThreadExecutor()

    private fun addEventListeners() = frame.addKeyListener(object : KeyListener {
        override fun keyTyped(e: KeyEvent?) {

        }

        override fun keyPressed(e: KeyEvent?) = when (e?.keyCode) {
            VK_LEFT -> car.controls.left = true
            VK_UP -> car.controls.forward = true
            VK_RIGHT -> car.controls.right = true
            VK_DOWN -> car.controls.reverse = true
            else -> Unit
        }

        override fun keyReleased(e: KeyEvent?) = when (e?.keyCode) {
            VK_LEFT -> car.controls.left = false
            VK_UP -> car.controls.forward = false
            VK_RIGHT -> car.controls.right = false
            VK_DOWN -> car.controls.reverse = false
            else -> Unit
        }
    })

    fun start() {
        running = true
        addEventListeners()
        Timer(1000 / FPS) {
            run()
        }.start()
    }

    val g get() = canvas.graphics
    val g2d = g.create() as Graphics2D

    fun run() {
        car.update()

        g2d.clearRect(0, 0, canvas.width, canvas.height)
        road.draw(g2d)
        car.draw(g2d)
    }

    fun stop() {
        running = false
    }
}