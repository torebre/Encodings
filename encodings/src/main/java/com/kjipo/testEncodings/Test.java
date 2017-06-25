package com.kjipo.testEncodings;


import com.kjipo.parser.FontFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.io.*;
import java.lang.reflect.InvocationTargetException;


public class Test {
	
	private static final Logger LOG = LoggerFactory.getLogger(Test.class);




	public static void setupEditorFrame(final String pTextToDisplay) throws InvocationTargetException, InterruptedException {

		SwingUtilities.invokeAndWait(() -> {


            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container contentPane = frame.getContentPane();
            JTextPane textPane = new JTextPane();

            contentPane.add(textPane);

            frame.pack();


            textPane.setContentType("text/html; JAPANESE_CHARSET=EUC-JP");

            textPane.setText(pTextToDisplay);

            System.out.println("Test1");

            frame.setVisible(true);

            System.out.println("Test2");

        });

	}


	public static void testTitle(final String pTitle) throws InvocationTargetException, InterruptedException {
		//		final String title = "Testing: \u30CD";
		SwingUtilities.invokeAndWait(new Runnable() {

			public void run() {
				JFrame frame = new JFrame(pTitle);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JLabel label = new JLabel(pTitle);
				label.setSize(200, 100);
				frame.setContentPane(label);
				frame.pack();
				frame.setVisible(true);
			}
		});


	}
	

	
	
	public static boolean[][] loadFont(String pTestCharacter) throws FontFormatException, IOException {
		File fontFile = new File("/home/student/edict/kochi-substitute-20030809/kochi-mincho-subst.ttf");
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
		
//		AffineTransform identityTransformation = new AffineTransform();
		
		FontRenderContext renderContext = new FontRenderContext(null, false, false);
		
		GlyphVector glyphVector = font.createGlyphVector(renderContext, pTestCharacter);
		
		System.out.println("Number of glyphs: " +glyphVector.getNumGlyphs());
		
//		for(int i = 0; i < glyphVector.getNumGlyphs(); ++i) {
//			Shape shape = glyphVector.getGlyphOutline(i);
//			PathIterator pathIterator = shape.getPathIterator(null);
//
//			while(!pathIterator.isDone()) {
//				double values[] = new double[6];
//
//				System.out.println(pathIterator.currentSegment(values));
//				for(double d : values) {
//					System.out.println(d +" ");
//				}
//				System.out.println();
//
//
//				pathIterator.next();
//
//			}
//
//
//		}

		return FontFileParser.setupRaster(glyphVector, 100, 100);
		
		
	}


    public static void paintOnRaster(GlyphVector pGlyphVector, int pRows, int pColumns) {
        boolean raster[][] = new boolean[pRows][pColumns];


        LOG.info("Test1");


        for(int i = 0; i < pGlyphVector.getNumGlyphs(); ++i) {

            LOG.info("Test2");

            Shape shape = pGlyphVector.getGlyphOutline(i);

            LOG.info("Bounds {}, {}, {}, {}", shape.getBounds().getMinX(), shape.getBounds().getMaxX(), shape.getBounds().getMinY(), shape.getBounds().getMaxY());

            PathIterator pathIterator = shape.getPathIterator(null);
//            double currentX = 0;
//            double currentY = 0;

            Point currentPoint = new Point(0, 0);
            Point lastMoveTo = currentPoint;
            double values[] = new double[6];


            while(!pathIterator.isDone()) {

//                SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE
                switch(pathIterator.currentSegment(values)) {
                    case PathIterator.SEG_MOVETO:

//                        currentX = values[0];
//                        currentY = values[0];

                        // TODO
                        translateToPoint(values[0], values[1], pRows, pColumns);

                        lastMoveTo = new Point(values[0], values[1]);
                        currentPoint = lastMoveTo;

                        LOG.info("Move to {}", currentPoint);

                        break;

                    case PathIterator.SEG_LINETO:

                        // TODO Draw line
                        currentPoint = new Point(values[0], values[1]);

                        LOG.info("Line to {}", currentPoint);

                        break;


                    case PathIterator.SEG_QUADTO:

                        currentPoint = addPoints(currentPoint, quadTo(currentPoint,
                                new Point(values[0], values[1]),
                                new Point(values[2], values[3])));


                        LOG.info("Quad to {}", currentPoint);


                        break;

                    case PathIterator.SEG_CUBICTO:

                        currentPoint = addPoints(currentPoint, cubicTo(currentPoint,
                                new Point(values[0], values[1]),
                                new Point(values[2], values[3]),
                                new Point(values[4], values[5])));


                        LOG.info("Cubic to {}", currentPoint);


                        break;


                    case PathIterator.SEG_CLOSE:

                        LOG.info("Segment close");

                        break;

                }

//                System.out.println(pathIterator.currentSegment(values));
//                for(double d : values) {
//                    System.out.println(d +" ");
//                }
//                System.out.println();


                pathIterator.next();

            }

            LOG.info("Layout flags: {}", pGlyphVector.getLayoutFlags());


//            RasterUtilities.paintOnRaster(shape);

        }



    }






    private static class Point {
        private final double x;
        private final double y;


        Point(double pX, double pY) {
            x = pX;
            y = pY;
        }

        @Override
        public String toString() {
            return "x: " +x +". y: " +y;
        }

    }


    private static void translateToPoint(double pX, double pY, int pRows, int pColumns) {
        // TODO





    }


//    P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
//    0 <= t <= 1
//
//    B(n,m) = mth coefficient of nth degree Bernstein polynomial
//    = C(n,m) * t^(m) * (1 - t)^(n-m)
//    C(n,m) = Combinations of n things, taken m at a time
//            = n! / (m! * (n-m)!)


    private static Point cubicTo(Point pCp, Point pP1, Point pP2, Point pP3) {
        Point start = getPointAtValue(pCp, pP1, pP2, pP3, 0);
        Point end = getPointAtValue(pCp, pP1, pP2, pP3, 1);

        // TODO

        LOG.info("Start: {}. End: {}", start, end);

        return end;
    }


//    P(t) = B(2,0)*CP + B(2,1)*P1 + B(2,2)*P2
//    0 <= t <= 1
//
//    B(n,m) = mth coefficient of nth degree Bernstein polynomial
//    = C(n,m) * t^(m) * (1 - t)^(n-m)
//    C(n,m) = Combinations of n things, taken m at a time
//            = n! / (m! * (n-m)!)

    private static Point quadTo(Point pCp, Point pP1, Point pP2) {
        Point start = quadTo(pCp, pP1, pP2, 0);
        Point end = quadTo(pCp, pP1, pP2, 1);

        LOG.info("Start: {}. End: {}", start, end);


        return end;

    }

    private static Point quadTo(Point pCp, Point pP1, Point pP2, float pT) {
        return getPointAtValue(pCp, pP1, pP2, pT);

    }


    private static Point getPointAtValue(Point pCp, Point pP1, Point pP2, float pT) {
        return addPoints(multiplyPointByConstant(getCoefficient(2, 0, pT), pCp),
                multiplyPointByConstant(getCoefficient(2, 1, pT), pP1),
                multiplyPointByConstant(getCoefficient(2, 2, pT), pP2));
    }

    private static Point getPointAtValue(Point pCp, Point pP1, Point pP2, Point pP3, float pT) {
        return addPoints(multiplyPointByConstant(getCoefficient(3, 0, pT), pCp),
                multiplyPointByConstant(getCoefficient(3, 1, pT), pP1),
                multiplyPointByConstant(getCoefficient(3, 2, pT), pP2),
                multiplyPointByConstant(getCoefficient(3, 3, pT), pP3));
    }

    private static Point addPoints(Point... pPoints) {
        double x = 0;
        double y = 0;
        for(Point point : pPoints) {
            x += point.x;
            y += point.y;
        }
        return new Point(x, y);
    }

    private static Point multiplyPointByConstant(float pConstant, Point pPoint) {
        return new Point(Math.round(pConstant * pPoint.x),
                Math.round(pConstant * pPoint.y));
    }


    //    B(n,m) = mth coefficient of nth degree Bernstein polynomial

    private static float getCoefficient(int pN, int pM, float pT) {
        return Double.valueOf(getCombinations(pN, pM) * Math.pow(pT, pM) * Math.pow(pT, pN)).floatValue();
    }

    private static int getCombinations(int pN, int pM) {

//        LOG.info("n: {}. m: {}", pN, pM);

        return getFactorial(pN) / (getFactorial(pM) * getFactorial(pN - pM));
    }


    private static int getFactorial(int pN) {
        if(pN == 0) {
            return 1;
        }
        for(int i = pN - 1; i > 1; --i) {
            pN *= i;
        }
        return pN;
    }


    public static boolean[][] getTestCharacter() {
        InputStreamReader input = null;
        try {
            input = new InputStreamReader(
                    new BufferedInputStream(new FileInputStream(new File("/home/student/edict/edict2"))), "EUC-JP");
            BufferedReader reader = new BufferedReader(input);
            String testLine = reader.readLine();
            String firstCharacter = testLine.substring(0, 1);
            System.out.println(firstCharacter);
            return loadFont(firstCharacter);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (FontFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


	public static void main(String pArgs[]) throws Exception {

		//		System.out.println(Charset.availableCharsets());



		InputStreamReader input = new InputStreamReader(
				new BufferedInputStream(new FileInputStream(new File("/home/student/edict/edict2"))), "EUC-JP");
		
		
		BufferedReader reader = new BufferedReader(input);
		
		String testLine = reader.readLine();


//		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//
//		OutputStreamWriter output = new OutputStreamWriter(byteOutput, "EUC-JP");
//
//		PrintStream out = new PrintStream(System.out, true, "UTF-8"); // "EUC-JP");
//
//
//
//		int character;
//		int counter = 0;
//		while((character = input.read()) != -1 && counter < 10) {
//			output.write(character);
//			++counter;
//
//
//			//			out.print((char)character);
//
//
//		}
//
//		output.flush();
//		output.close();
//		input.close();

		
		String firstCharacter = testLine.substring(0, 1);
		System.out.println(firstCharacter);
		
//		testTitle(byteOutput.toString("EUC-JP"));
		
//		setupEditorFrame(byteOutput.toString("EUC-JP"));
		
//		loadFont(firstCharacter);


        loadFont(firstCharacter);



	}


}
