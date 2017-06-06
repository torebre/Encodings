package raster;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.kjipo.setupUtilities.RasterUtilities;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.*;

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


    private GlyphVector getGlyphForCharacter(String pTestCharacter) {
        FontRenderContext renderContext = new FontRenderContext(null, false, false);
        return testFont.createGlyphVector(renderContext, pTestCharacter);
    }

    private boolean[][] createRasterForCharacter(String pTestCharacter) {
        return RasterUtilities.setupRaster(getGlyphForCharacter(pTestCharacter), 100, 100);
    }

    @Test
    public void testEnergyFlow() {
        BooleanEncoding booleanEncoding = new BooleanEncoding(createRasterForCharacter(testCharacter));
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

    @Test(enabled = false)
    public void testFont() throws IOException, FontFormatException {
        InputStream fontStream = getClass().getResourceAsStream("/font/kochi-mincho-subst.ttf");
        Font testFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        fontStream.close();
        InputStreamReader input = new InputStreamReader(
                new BufferedInputStream(new FileInputStream(new File("/home/student/edict/edict2"))), "EUC-JP");
        BufferedReader reader = new BufferedReader(input);
        String line;

        int counter = 0;

        while((line = reader.readLine()) != null) {

            String character = line.substring(0, line.indexOf('/'));

            System.out.println("Character " +counter++ +": " + character);

        }

    }

    @Test
    public void testDirection() {
        Assert.assertEquals(BooleanEncodingIterator.getDirection(0), FlowDirections.EAST);
        Assert.assertEquals(BooleanEncodingIterator.getDirection(Math.PI / 16), FlowDirections.EAST);
        Assert.assertEquals(BooleanEncodingIterator.getDirection(Math.PI / 8), FlowDirections.NORTH_EAST);
        Assert.assertEquals(BooleanEncodingIterator.getDirection(Math.PI / 2), FlowDirections.NORTH);
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


}
