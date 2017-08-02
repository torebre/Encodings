package com.kjipo.raster.stochasticflow;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Chars;
import com.kjipo.parser.FontFileParser;
import com.kjipo.parser.KanjiDicParser;
import com.kjipo.parser.Parsers;
import com.kjipo.raster.filter.Filter;
import com.kjipo.raster.filter.MaskFilter;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.RasterVisualizer2;
import com.kjipo.visualization.stochasticflow.FlowPainter;
import com.kjipo.visualization.stochasticflow.FlowStrengthPainter;


import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
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

        boolean[][] rawImage = kanji.getImage();


//        Filter skeletonFilter = new SkeletonFilter();
//        boolean filteredImage[][] = skeletonFilter.applyFilter(rawImage);

        Filter maskFilter = new MaskFilter();
        List<boolean[][]> filteredImages = maskFilter.applyFilter(rawImage);

//        List<boolean[][]> filteredImages = Collections.singletonList(rawImage);

        java.util.List<StochasticFlowRasterImpl> run = runStochasticFlow.createRun(filteredImages.get(filteredImages.size() - 1));

        RasterVisualizer2.showRasterFlow(RasterRunCreator.createRasterRun(run, kanji.getImage().length,
                kanji.getImage()[0].length, filteredImages.get(filteredImages.size() - 1)),
                ImmutableList.of(new FlowPainter(), new FlowStrengthPainter()));
    }


    public static void main(String args[]) throws Exception {
        TestStochasticFlow test = new TestStochasticFlow();
        test.stochasticFlowTest();

    }


}
