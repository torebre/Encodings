package com.kjipo.raster.flow;


import com.kjipo.raster.TileType;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.*;

import static com.kjipo.parser.FontFileParser.setupRaster;

public class TestBooleanEncoding {
    private Font testFont;
    private String testCharacter;
    private static final double EPSILON = 1e-6;
    private static final Logger LOG = LoggerFactory.getLogger(TestBooleanEncoding.class);


    @BeforeClass
    public void beforeClass() throws IOException, FontFormatException {
            InputStream fontStream = getClass().getResourceAsStream("/font/kochi-mincho-subst.ttf");
            testFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        fontStream.close();
        InputStreamReader input = new InputStreamReader(
                new BufferedInputStream(new FileInputStream(new File("/home/student/edict/edict2"))), "EUC-JP");
        BufferedReader reader = new BufferedReader(input);
        testCharacter = reader.readLine().substring(0, 1);
    }




    @Test
    public void testEnergyDistribution() {
        BooleanEncoding booleanEncoding = new BooleanEncoding(createRasterForCharacter(testCharacter));
        Complex complex = ComplexUtils.polar2Complex(1, Math.PI / 8);
        Complex energy[] = BooleanEncodingIterator.distributeEnergy(complex);
        for(Complex c : energy) {
            System.out.println("Complex: " +c);
        }
    }

    @Test
    public void testEnergyFraction() {
        BooleanEncoding booleanEncoding = new BooleanEncoding(createRasterForCharacter(testCharacter));
        Assert.assertEquals(BooleanEncodingIterator.calculateEnergy(0, Math.PI / 2), 0.5, EPSILON);
        Assert.assertEquals(BooleanEncodingIterator.calculateEnergy(0, 3 * Math.PI / 2), 0.5, EPSILON);
        Assert.assertEquals(BooleanEncodingIterator.calculateEnergy(0, Math.PI / 4), 0.25, EPSILON);
    }

    @Test
    public void testUpdateFlow2() {
        Complex flowRaster[][] = new Complex[3][3];
        Complex updatedFlowRaster[][] = new Complex[3][3];
        boolean raster[][] = new boolean[3][3];

        for(int i = 0; i < raster.length; ++i) {
            for(int j = 0; j < raster[0].length; ++j) {
                flowRaster[i][j] = Complex.ZERO;
                updatedFlowRaster[i][j] = Complex.ZERO;
            }
        }
        raster[0][0] = true;
        BooleanEncodingIterator.updateFlow2(1, 1, ComplexUtils.polar2Complex(1, 2 * Math.PI - Math.PI / 4), flowRaster, updatedFlowRaster, raster);

        for(int i = 0; i < raster.length; ++i) {
            for(int j = 0; j < raster[0].length; ++j) {
                System.out.print(updatedFlowRaster[i][j] +"\t");
            }
            System.out.println();
        }

    }

    @Test
    public void testDissipateEnergy() {
        TileType neighbours[] = new TileType[8];
        for(int i = 0; i < neighbours.length; ++i) {
            neighbours[i] = TileType.OUTSIDE_SCREEN;
        }
        neighbours[0] = TileType.OPEN;
        double energies[] = BooleanEncodingIterator.dissipateEnergy(Complex.ONE, neighbours);

        System.out.println("Energies:");
        for(double d : energies) {
            System.out.println(d);
        }

    }

    private GlyphVector getGlyphForCharacter(String pTestCharacter) {
        FontRenderContext renderContext = new FontRenderContext(null, false, false);
        return testFont.createGlyphVector(renderContext, pTestCharacter);
    }

    private boolean[][] createRasterForCharacter(String pTestCharacter) {
        return setupRaster(getGlyphForCharacter(pTestCharacter), 100, 100);
    }



    }
