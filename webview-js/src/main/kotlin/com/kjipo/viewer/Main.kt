package com.kjipo.viewer


fun main(args: Array<String>) {
    val kanjiApp = KanjiApp()

    kanjiApp.setupKanjiSelection()
    kanjiApp.selectAndLoad(33541)

}