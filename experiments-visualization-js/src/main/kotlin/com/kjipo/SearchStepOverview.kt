package com.kjipo

import com.kjipo.experiments.SearchDescription
import createAndAddButton
import getColour
import kotlinx.browser.document


class SearchStepOverview(
    searchDescription: SearchDescription,
    searchOverview: SearchOverview,
    inputSampleVisualization: InputSampleVisualization,
    parentElement: String
) {

    private var currentlyPressedButtonId: String? = null

    init {
        val element = document.getElementById(parentElement)!!
        searchDescription.nextInput.forEach {
            val inputStepButton = createAndAddButton(
                "input-${it.stepId}", "Input ${it.stepId}"
            ) { _ ->
                markInputStep(it.stepId, inputSampleVisualization, searchDescription)
            }
            element.appendChild(inputStepButton)
        }

        searchDescription.searchPlayThrough.map { it.stepId }.toSet().forEach { stepIndex ->
            document.createElement("button").also { button ->
                val buttonId = "show-step-${stepIndex}"
                button.setAttribute("id", buttonId)
                element.appendChild(button)
                button.addEventListener("click", {
                    searchOverview.showStep(stepIndex)

                    // TODO
//                    showStep(stepIndex)
                    currentlyPressedButtonId?.let { pressedButtonId ->
                        document.getElementById(pressedButtonId)?.removeAttribute("class")
                    }
                    button.setAttribute("class", "clicked-button")
                    currentlyPressedButtonId = buttonId
                })

                val buttonText = document.createTextNode("Step $stepIndex")
                button.appendChild(buttonText)
            }
        }

        val pathParentElementId = "input-steps"
        document.createElement("div").also {
            it.setAttribute("id", pathParentElementId)
            element.appendChild(it)
        }
        searchDescription.nextInput.forEach { inputLinePair ->
            val inputSampleVisualization =
                InputSampleVisualization(
                    numberOfRows,
                    numberOfColumns,
                    pathParentElementId,
                    searchDescription.inputSample
                )
            inputSampleVisualization.markLines(listOf(inputLinePair.line1Id, inputLinePair.line2Id), 4, getColour(4))
        }
    }

    private fun markInputStep(
        inputStep: Int,
        inputSampleVisualization: InputSampleVisualization,
        searchDescription: SearchDescription
    ) {
        inputSampleVisualization.colourLineCollection(
            listOf(
                searchDescription.nextInput[inputStep].line1Id,
                searchDescription.nextInput[inputStep].line2Id
            )
        )
    }


}