package com.kjipo.raster.stochasticflow;

import com.google.common.primitives.Chars;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.representation.EncodedKanji;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import visualization.RasterVisualizer2;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

public class RunStochasticFlowTest {

    private static final Logger LOG = LoggerFactory.getLogger(RunStochasticFlowTest.class);


    private void createRunTest() throws IOException, FontFormatException {
        RunStochasticFlow runStochasticFlow = new RunStochasticFlow();

        java.util.List<KanjiDicParser.KanjiDicEntry> entries = KanjiDicParser.parseKanjidicFile(Parsers.EDICT_FILE_LOCATION).collect(Collectors.toList());
        KanjiDicParser.KanjiDicEntry kanjiDicEntry = entries.get(0);

        char[] chars1 = kanjiDicEntry.getKanji().toCharArray();

        Collection<EncodedKanji> encodedKanjis;
        try (InputStream fontStream = new FileInputStream(Parsers.FONT_FILE_LOCATION.toFile())) {
            encodedKanjis = FontFileParser.parseFontFile(Chars.asList(chars1).stream()
                    .map(Character::new)
                    .collect(Collectors.toList()), fontStream);
        }

        EncodedKanji kanji = encodedKanjis.iterator().next();
        java.util.List<StochasticFlowRasterImpl> run = runStochasticFlow.createRun(kanji.getImage());


        int counter = 0;
        for (StochasticFlowRasterImpl stochasticFlowRaster : run) {
            int cellsWithFlow = 0;
            for(int row = 0; row < stochasticFlowRaster.getRows(); ++row) {
                for(int column = 0; column < stochasticFlowRaster.getColumns(); ++column) {
                    if (stochasticFlowRaster.getFlowInCell(row, column) > 0) {
//                        LOG.info("Found cell with flow at {}, {}", row, column);
                        ++cellsWithFlow;
                    }
                }
            }
            LOG.info("Cells with flow at step {}: {}", counter, cellsWithFlow);

            ++counter;
        }

    }


    public static void main(String args[]) throws IOException, FontFormatException {
        RunStochasticFlowTest test = new RunStochasticFlowTest();
        test.createRunTest();


    }


}
