package com.kjipo.segmentation;

import com.kjipo.representation.prototype.Prototype;
import com.kjipo.raster.Cell;
import com.kjipo.raster.attraction.SegmentMatcher;
import com.kjipo.raster.match.MatchTest;
import com.kjipo.representation.segment.Pair;
import com.kjipo.representation.segment.Segment;
import com.kjipo.recognition.RecognitionUtilities;
import com.kjipo.representation.EncodedKanji;
import com.kjipo.visualization.segmentation.SegmentationVisualizer;
import javafx.scene.paint.Color;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.List;

public class KanjiSegmenterTest {
    private static final Logger logger = LoggerFactory.getLogger(KanjiSegmenterTest.class);


    @Test
    public void segmentationTest() throws IOException, ClassNotFoundException, InterruptedException {
//        boolean prototype[][] = new boolean[10][10];
//        for (int i = 0; i < 6; ++i) {
//            prototype[8][i + 2] = true;
//        }
//        EncodedKanji encodedKanji = new EncodedKanji(prototype);

        EncodedKanji encodedKanji;
        try (InputStream fontStream = new FileInputStream(Paths.get("/home/student/test_kanji.xml").toFile());
             ObjectInputStream objectInputStream = new ObjectInputStream(fontStream)) {
            encodedKanji = (EncodedKanji) objectInputStream.readObject();
        }

        Cell[][] flowRaster = RasterTransformer.segmentTransformer(encodedKanji.getImage());

        List<Segment> segments = KanjiSegmenter.segmentKanji(flowRaster);

        logger.info("Number of segments: {}", segments.size());

        double red = 0.0;
        double blue = 0.0;

        double deltaRed = 1.0 / segments.size();
        double deltaBlue = 1.0 / segments.size();

        Color colorRaster[][] = new Color[flowRaster.length][flowRaster[0].length];
        for (Segment segment : segments) {
            Color segmentColor = Color.color(red, blue, 1.0);

            for (Pair pair : segment.getPairs()) {
                colorRaster[pair.getRow()][pair.getColumn()] = segmentColor;
            }

            red += deltaRed;
            blue += deltaBlue;
        }


        Prototype testPrototype2 = MatchTest.getTestPrototype2();
        Segment inputSegmentData = segments.get(0);
        List<List<Segment>> segmentLines = SegmentMatcher.positionPrototype(flowRaster.length, flowRaster[0].length,
                inputSegmentData, testPrototype2);

        List<Segment> joinedSegmentLines = RecognitionUtilities.joinSegmentLines(segmentLines);

//        List<Pair> prototypeSegments = testPrototype2.getSegments().stream().map(Segment::getPairs).flatMap(Collection::stream).collect(Collectors.toList());


        SegmentationVisualizer.showRasterFlow(encodedKanji, colorRaster, inputSegmentData.getPairs(), joinedSegmentLines);

        Thread.sleep(Long.MAX_VALUE);

    }




}
