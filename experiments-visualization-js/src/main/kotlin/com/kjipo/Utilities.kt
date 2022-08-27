import com.kjipo.representation.LineUtilities
import com.kjipo.representation.prototype.LinePrototype
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.events.Event

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


fun createAndAddButton(buttonId: String, buttonText: String, clickCallback: (Event) -> Unit): Element {
    return document.createElement("button").also { button ->
        button.setAttribute("id", buttonId)
        button.addEventListener("click", clickCallback)
        val buttonText = document.createTextNode(buttonText)
        button.appendChild(buttonText)
    }
}

fun getColour(value: Int): String {
    return when (value) {
        4 -> {
           "green"
        }
        3 -> {
            "yellow"
        }
        2 -> {
            "red"
        }

        1 -> {
            "blue"
        }

        else -> {
            "white"
        }
    }
}
