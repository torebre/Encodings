package com.kjipo.experiments

import java.nio.file.Files
import java.nio.file.Paths

object ReadEtlData {

    const val BASE_FILE_NAME = "/home/student/Downloads/ETL/ETL9B/ETL9B_"
    private const val STRUCT_LENGTH = 576


    private fun readData() {
        for(i in 1..6) {
            val file = Paths.get(BASE_FILE_NAME +i)

            val allInputData = Files.readAllBytes(file)

            val byteIterator = allInputData.iterator()

            for(j in 0..STRUCT_LENGTH) {
                byteIterator.nextByte()
            }


            while(byteIterator.hasNext()) {

                println("Reading record")

                val record = ByteArray(STRUCT_LENGTH)
                for(j in 0 until STRUCT_LENGTH) {
                    record[j] = byteIterator.nextByte()
                }

                val serialSheetNumber = record[0].toInt().shl(8).plus(record[1])
                val jisKanjiCode = record[2].toInt().shl(8).plus(record[3])


                println("Serial sheet number: $serialSheetNumber. JIS kanji code: $jisKanjiCode")


                val pixelByte = record[8]



            }





//            # The first record is a dummy record
//            file.read(structLength)
//
//            while True:
//            record = file.read(structLength)
//
//            if not record:
//            break
//
//            (serialSheetNumber, kanjiCode, typicalReading, imageData, uncertain) = unpack_function(record)
//            image = np.unpackbits(np.fromstring(imageData, dtype=np.uint8)).reshape((63, 64))
//
//            kanjiData.append({'serialSheetNumber': serialSheetNumber, 'kanjiCode': kanjiCode, 'typicalReading': typicalReading, 'image': image})



        }




    }




    @JvmStatic
    fun main(args: Array<String>) {
        readData()

    }

}