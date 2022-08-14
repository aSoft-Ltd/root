package sdc

class Road(
    val centerX: Double,
    val width: Double,
    val laneCount: Int = 3
) {
    val left = centerX - width / 2
    val right = centerX + width / 2

    private val infinity = 10000.0

    val top = infinity
    val bottom = -infinity

    val lineWidth = 5f

    fun getCenterLane(lane: Int): Double {
        if (lane > laneCount) throw IndexOutOfBoundsException("Lane count is limited only to $laneCount")
        val laneWidth = width / laneCount
        return left + laneWidth / 2 + (lane * laneWidth)
    }
}