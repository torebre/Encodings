package com.kjipo.experiments

import java.nio.charset.Charset

object WriteCharset {


    private fun printCharset() {
        val charset = Charset.forName("x-JIS0208")

        val decoder = charset.newDecoder()

        // TODO



    }


    @JvmStatic
    fun main(args: Array<String>) {
        printCharset()

    }





}