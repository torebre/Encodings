package com.kjipo.datageneration

import com.kjipo.representation.prototype.LinePrototype
import com.kjipo.representation.segment.Pair
import kotlin.math.*
import kotlin.random.Random


class LinePrototypeWithAngle(startPair: Pair,
stopPair: Pair, val angle: Double, val length: Double): LinePrototype(startPair, stopPair) {

}



object CreateSamples {

    private val random = Random.Default

    fun generateSample(includeRectangle: Boolean,
        numberOfRows: Int, numberOfColumns: Int, numberOfRandomLines: Int
    ): List<LinePrototypeWithAngle> {
        val sample = mutableListOf<LinePrototypeWithAngle>()

        // Include four more lines if no rectangle should be drawn, so that the number of
        // lines returned is the same
        val numberOfLines = numberOfRandomLines + if(!includeRectangle) 4 else 0

//        training_samples = np.zeros((number_of_random_lines + 4, 6))
//
//        if not include_rectangle:
//        # Include four more random lines so that the returned number of lines is always the same
//                number_of_random_lines += 4
//
//        for i in range(0, number_of_random_lines):
//        # TODO Check if end is inclusive for range
//        start_x = random.sample(range(0, number_of_rows), 1)[0]
//        start_y = random.sample(range(0, number_of_columns), 1)[0]


        for(i in 0 until numberOfLines) {
            val xStart = random.nextInt(0, numberOfRows)
            val yStart = random.nextInt(0, numberOfColumns)

            val xStop = random.nextInt(0, numberOfRows)
            val yStop = random.nextInt(0, numberOfColumns)
//
//        stop_x = random.sample(range(0, number_of_rows), 1)[0]
//        stop_y = random.sample(range(0, number_of_columns), 1)[0]
//
//        x_delta = stop_x - start_x
//        y_delta = stop_y - start_y

            val xDelta = xStop - xStart
            val yDelta = yStop - yStart

//        # TODO Check that this is the correct function to use
//        angle = math.atan2(y_delta, x_delta)
//        line_length = math.sqrt(x_delta * x_delta + y_delta * y_delta)
            val angle = atan2(yDelta.toDouble(), xDelta.toDouble())
            val length = sqrt(xDelta.toDouble() * xDelta + yDelta * yDelta)

            sample.add(
                LinePrototypeWithAngle(
                    Pair(xStart, yStart),
                    Pair(xStop, yStop),
                    angle,
                    length
                )
            )
        }

//
//        training_samples[i, 0] = angle
//        training_samples[i, 1] = line_length
//        training_samples[i, 2] = start_x
//        training_samples[i, 3] = start_y
//        training_samples[i, 4] = stop_x
//        training_samples[i, 5] = stop_y
//
//        if include_rectangle:
//        training_samples[number_of_random_lines:(number_of_random_lines + 4), :] = add_rectangle(number_of_rows,
//        number_of_columns)

        if(includeRectangle) {
            sample.addAll(addRectangle(numberOfRows, numberOfColumns))
        }

        return sample
    }


    fun addRectangle(numberOfRows: Int, numberOfColumns: Int): List<LinePrototypeWithAngle> {

//    def add_rectangle(number_of_rows: int = 64, number_of_columns: int = 64) -> npt.ArrayLike:
//    radius = math.floor(random.sample(range(1, min([number_of_rows, number_of_columns])), 1)[0] / 2)

        val radius = floor(random.nextInt(1, min(numberOfRows, numberOfColumns))/2.0).toInt()
//
//    x_centre = random.sample(range(radius, number_of_rows - radius), 1)[0]
//    y_centre = random.sample(range(radius, number_of_columns - radius), 1)[0]

        val xCenter = random.nextInt(radius, numberOfRows - radius)
        val yCenter = random.nextInt(radius, numberOfColumns - radius)

//
//    rotation = random.random() * math.pi / 2
//    point_rotation = (math.pi / 2 - rotation) * random.random()

        val rotation = random.nextDouble() * PI / 2.0
        val pointRotation = (PI / 2.0 - rotation) * random.nextDouble()

//
//    first_point_rotation = rotation + point_rotation
//    second_point_rotation = math.pi - point_rotation + rotation

        val firstPointRotation = rotation + pointRotation
        val secondPointRotation = PI - pointRotation + rotation

//
//    third_point_rotation = rotation - point_rotation
//    fourth_point_rotation = rotation + math.pi + (math.pi - second_point_rotation + rotation)

        val thirdPointRotation = rotation - pointRotation
        val fourthPointRotation = rotation + PI + (PI - secondPointRotation + rotation)
//
//    x1 = math.cos(first_point_rotation) * radius + x_centre
//    y1 = math.sin(first_point_rotation) * radius + y_centre

        val x1 = (cos(firstPointRotation) * radius + xCenter).toInt()
        val y1 = (sin(firstPointRotation) * radius + yCenter).toInt()

//
//    x2 = math.cos(second_point_rotation) * radius + x_centre
//    y2 = math.sin(second_point_rotation) * radius + y_centre

        val x2 = (cos(secondPointRotation) * radius + xCenter).toInt()
        val y2 = (sin(secondPointRotation) * radius + yCenter).toInt()

//
//    x3 = math.cos(third_point_rotation) * radius + x_centre
//    y3 = math.sin(third_point_rotation) * radius + y_centre

        val x3 = (cos(thirdPointRotation) * radius + xCenter).toInt()
        val y3 = (sin(thirdPointRotation) * radius + yCenter).toInt()
//
//    x4 = math.cos(fourth_point_rotation) * radius + x_centre
//    y4 = math.sin(fourth_point_rotation) * radius + y_centre

        val x4 = (cos(fourthPointRotation) * radius + xCenter).toInt()
        val y4 = (sin(fourthPointRotation) * radius + yCenter).toInt()

//
//    length_12 = math.sqrt(math.pow(abs(x2 - x1), 2) + math.pow(abs(y2 - y1), 2))
//    angle_12 = rotation + math.pi

        val length12 = sqrt(abs(x2 - x1).toDouble().pow(2.0) + abs(y2 - y1).toDouble().pow(2.0))
        val angle12 = rotation + PI

//
//    length_24 = math.sqrt(math.pow(abs(x2 - x4), 2) + math.pow(abs(y2 - y4), 2))
//    angle_24 = rotation - math.pi / 2

        val length24 = sqrt(abs(x2 - x4).toDouble().pow(2.0) + abs(y2 - y4).toDouble().pow(2.0))
        val angle24 = rotation - PI / 2.0

//
//    length_13 = math.sqrt(math.pow(abs(x3 - x1), 2) + math.pow(abs(y3 - y1), 2))
//    angle_13 = rotation - math.pi / 2

        val length13 = sqrt(abs(x3 - x1).toDouble().pow(2.0) + abs(y3 - y1).toDouble().pow(2.0))
        val angle13 = rotation - PI / 2.0

//
//    length_34 = math.sqrt(math.pow(abs(x3 - x4), 2) + math.pow(abs(y3 - y4), 2))
//    angle_34 = math.pi + rotation

        val length34 = sqrt(abs(x3 - x4).toDouble().pow(2.0) + abs(y3 - y4).toDouble().pow(2.0))
        val angle34 = PI + rotation

        return listOf(
            LinePrototypeWithAngle(Pair(x1, y1), Pair(x2, y2), angle12, length12),
            LinePrototypeWithAngle(Pair(x2, y2), Pair(x4, y4), angle24, length24),
            LinePrototypeWithAngle(Pair(x1, y1), Pair(x3, y3), angle13, length13),
            LinePrototypeWithAngle(Pair(x3, y3), Pair(x4, y4), angle34, length34))

//
//    return np.array([[angle_12, length_12, x1, y1, x2, y2],
//    [angle_24, length_24, x2, y2, x4, y4],
//    [angle_13, length_13, x1, y1, x3, y3],
//    [angle_34, length_34, x3, y3, x4, y4]])


    }


}