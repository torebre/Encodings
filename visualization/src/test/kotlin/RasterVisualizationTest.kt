import com.kjipo.parser.FontFileParser
import com.kjipo.parser.Parsers
import com.kjipo.prototype.CreatePrototypeDataset
import com.kjipo.prototype.Prototype
import com.kjipo.representation.EncodedKanji
import com.kjipo.visualization.displayRasters
import javafx.scene.paint.Color
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class RasterVisualizationTest {

    val log = LoggerFactory.getLogger(RasterVisualizationTest::class.java)


    @Test
    fun visualizationTest() {
        val fittedPrototypes = Files.walk(Paths.get("fittedPrototypes3"))
                .filter { path -> Files.isRegularFile(path) }
                .collect(Collectors.toMap<Path, Int, Prototype>({
                    val fileName = it.fileName.toString()
                    Integer.valueOf(fileName.substring(0, fileName.indexOf('.')))
                }) {
                    Files.newInputStream(it).use {
                        CreatePrototypeDataset.readPrototype(it)
                    }
                })

        val encodedKanjis = FileInputStream(Parsers.FONT_FILE_LOCATION.toFile()).use({ fontStream ->
            FontFileParser.parseFontFileUsingUnicodeInput(fittedPrototypes.keys, fontStream, 200, 200).stream()
        }).collect(Collectors.toMap<EncodedKanji, Int, EncodedKanji>({
            it.character.toInt()
        }) {
            it
        })

        val texts = mutableListOf<String>()
        val colourRasters = fittedPrototypes.entries.map {
            val currentKanji = encodedKanjis.getOrDefault(it.key, EncodedKanji(' ', emptyArray()))
            val image = currentKanji.image
            texts.add(currentKanji.unicode.toString().plus(": ").plus(String(Character.toChars(currentKanji.unicode))))

            if(image.isEmpty()) {
                log.error("Image is empty")
            }

            val colourRaster = Array(image.size, { row ->
                Array(image[0].size, { column ->
                    if (image[row][column]) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                })
            })


            it.value.segments.forEach {
                it.pairs.forEach {
                    if(it.row >= colourRaster.size || it.column >= colourRaster[0].size) {
                        log.error("Prototype outside allowed range")
                    }
                    else {
                        colourRaster[it.row][it.column] = Color.RED
                    }
                }
            }

            colourRaster
        }.toList()


//        displayKanjis(encodedKanjis.values)
        displayRasters(colourRasters, texts)

        Thread.sleep(Long.MAX_VALUE)

    }


}