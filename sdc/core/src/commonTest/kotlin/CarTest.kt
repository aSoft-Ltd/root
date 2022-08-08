import math.MutablePoint2
import math.XY
import sdc.Car
import kotlin.test.Test

class CarTest {
    @Test
    fun should_be_able_to_update_the_car() {
        val car = Car(
            pos = MutablePoint2(200.0, 200.0),
            size = XY(30, 50),
        )
        println(car)
        car.controls.forward = true
        car.update()
        println(car)
    }
}