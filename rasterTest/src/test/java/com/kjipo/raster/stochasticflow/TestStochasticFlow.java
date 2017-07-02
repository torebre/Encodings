package com.kjipo.raster.stochasticflow;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Chars;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.representation.EncodedKanji;
import visualization.RasterVisualizer2;
import visualization.stochasticflow.FlowPainter;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class TestStochasticFlow {


    public void stochasticFlowTest() throws IOException, FontFormatException, InterruptedException {
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

        RasterVisualizer2.showRasterFlow(RasterRunCreator.createRasterRun(run, kanji.getImage().length, kanji.getImage()[0].length, kanji.getImage()),
                ImmutableList.of(new FlowPainter()));
    }


    public static void main(String args[]) throws Exception {
        TestStochasticFlow test = new TestStochasticFlow();
        test.stochasticFlowTest();

    }


}
