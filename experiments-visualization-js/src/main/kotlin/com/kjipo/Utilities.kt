import com.kjipo.representation.LineUtilities
import com.kjipo.representation.prototype.LinePrototype

fun createIndexLinePrototypeMap(linePrototypes: Collection<LinePrototype>): Map<Int, List<Pair<Int, Int>>> {
    return linePrototypes.mapIndexed { index, linePrototype ->
        Pair(
            index,
            LineUtilities.createLine(
                linePrototype.startPair.column,
                linePrototype.startPair.row,
                linePrototype.endPair.column,
                linePrototype.endPair.row
            )
        )
    }.toMap()
}


fun getColour(value: Int): String {
    return when (value) {
        2 -> {
            "red"
        }

        1 -> {
            "blue"
        }

        else -> {
            "black"
        }
    }
}
