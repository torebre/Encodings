package com.kjipo.raster.flow;


import com.kjipo.raster.EncodingUtilities;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.TileType;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;


public class BooleanEncodingIterator {

    private static final Logger LOG = LoggerFactory.getLogger(BooleanEncodingIterator.class);


    public static BooleanEncoding iterate(BooleanEncoding booleanEncoding) {
        return iterate(booleanEncoding, false);
    }

    public static BooleanEncoding iterate(BooleanEncoding booleanEncoding, boolean createCopy) {
        return createCopy ? updateFlow(new BooleanEncoding(booleanEncoding)) : updateFlow(booleanEncoding);
    }

    private static BooleanEncoding updateFlow(BooleanEncoding booleanEncoding) {
        Queue<Point> queue = BooleanEncodingUtilities.getPointsWithFlow(booleanEncoding);

        LOG.info("Points with flow: {}", queue.size());

        Complex updatedFlowRaster[][] = BooleanEncodingUtilities.setupFlowRaster(booleanEncoding.raster);

        for (Point point : queue) {
            Complex flow = booleanEncoding.flowRaster[point.getRow()][point.getColumn()];
            updateFlow2(point.getRow(), point.getColumn(), flow, booleanEncoding.flowRaster,
                    updatedFlowRaster, booleanEncoding.raster);
        }

        for (Source source : booleanEncoding.sources) {
            Complex flow = source.getFlow();
            updateFlow2(source.getRow(), source.getColumn(), flow, booleanEncoding.flowRaster,
                    updatedFlowRaster, booleanEncoding.raster);
        }
        booleanEncoding.flowRaster = updatedFlowRaster;

        return booleanEncoding;
    }

    public static void placeSource(BooleanEncoding booleanEncoding) {
        for (int row = 0; row < booleanEncoding.raster.length; ++row) {
            for (int column = 0; column < booleanEncoding.raster[0].length; ++column) {
                if (booleanEncoding.raster[row][column]
                        && !BooleanEncodingUtilities.isSourceAtPoint(row, column, booleanEncoding)) {
                    Source source = new Source(row, column, new Complex(1, 0));

                    LOG.info("Adding source {}", source);

                    booleanEncoding.sources.add(source);

                    return;
                }
            }
        }
    }

    static void updateFlow2(int row, int column, Complex flow, Complex flowRaster[][],
                            Complex updatedFlowRaster[][], boolean raster[][]) {
        TileType neighbourTypes[] = EncodingUtilities.determineNeighbourTypes(row, column, raster);
        double energies[] = dissipateEnergy(flow, neighbourTypes);

//        LOG.info("Row: {}. Column: {}", row, column);
//        for(int i = 0; i < neighbourTypes.length; ++i) {
//            LOG.info("Neigbour: {}. Energy: {}", neighbourTypes[i], energies[i]);
//        }


        double angle = 0;
        FlowDirection directions[] = FlowDirection.values();

        for (int i = 0; i < neighbourTypes.length; ++i) {
            double energy = energies[i];
            if (Double.isNaN(energy)) {
                angle += Math.PI / 4;
                continue;
            }

//            LOG.info("Row: {}. Column: {}. Row shift: {}. Column shift: {}. i: {}", row, column, directions[i].getRowShift(), directions[i].getColumnShift(), i);

            if (Double.compare(energy, 0.0) == 0) {
                updatedFlowRaster[row + directions[i].getRowShift()][column + directions[i].getColumnShift()] = flowRaster[row + directions[i].getRowShift()][column + directions[i].getColumnShift()];
                angle += Math.PI / 4;
                continue;
            }

//            LOG.info("Updating flow for {}, {}", row, column);

            Complex oldValue = flowRaster[row + directions[i].getRowShift()][column + directions[i].getColumnShift()];

//            LOG.info("Old value: {}", oldValue);
//            LOG.info("Angle: {}. Energy: {}", angle, energy);
//            LOG.info("Adding: {}", ComplexUtils.polar2Complex(energy, angle));

            updatedFlowRaster[row + directions[i].getRowShift()][column + directions[i].getColumnShift()] = oldValue.add(ComplexUtils.polar2Complex(energy, angle));

//            LOG.info("Updated flow for {}, {}: {}", row + directions[i].getRowShift(),
//                    column + directions[i].getColumnShift(),
//                    updatedFlowRaster[row + directions[i].getRowShift()][column + directions[i].getColumnShift()]);

            angle += Math.PI / 4;
        }
        updatedFlowRaster[row][column] = flowRaster[row][column].add(ComplexUtils.polar2Complex(Math.abs(energies[8]), flow.getArgument() + Math.PI));
    }


    /*
        The input starts with east in position 0
     */
    static double[] dissipateEnergy(Complex center, TileType neighbours[]) {
        FlowDirection direction = getDirection(center.getArgument());
        int cell = direction.ordinal();

//        LOG.info("Direction: {}", direction);

        Complex temp[] = distributeEnergy(center);
        Complex distributed[] = new Complex[neighbours.length];
        System.arraycopy(temp, 0, distributed, 0, temp.length);
//        distributed[0] = Complex.ZERO;
        distributed[distributed.length - 3] = Complex.ZERO;
        distributed[distributed.length - 2] = Complex.ZERO;
        distributed[distributed.length - 1] = Complex.ZERO;

        FlowDirection startDirection = getDirection(direction, -2);
        int shift = startDirection.ordinal();

        double result[] = new double[distributed.length + 1];
        for (int i = 0; i < result.length; ++i) {
            result[i] = 0;
        }

        for (int i = 0; i < neighbours.length; ++i) {
            int temp2 = i - shift;
            if (temp2 >= 8) {
                temp2 -= 8;
            } else if (temp2 < 0) {
                temp2 += 8;
            }

            switch (neighbours[i]) {

                // TODO Set back to something else than 0

                case OUTSIDE_SCREEN:
                    result[result.length - 1] = result[result.length - 1] + -distributed[temp2].abs();
                    result[i] = Double.NaN;
                    break;

                case OUTSIDE_CHARACTER:
                    result[i] = distributed[temp2].abs() * 0.3;
                    result[result.length - 1] = result[result.length - 1] + -distributed[temp2].abs() * 0.7;
                    break;

                case OPEN:
                    result[i] = distributed[temp2].abs();
                    break;

            }

        }

        return result;
    }

    static Complex[] distributeEnergy(Complex pFlow) {
        double length = pFlow.abs();
        double angle = fixAngle(pFlow.getArgument()); // Math.atan(pFlow.getImaginary() / pFlow.getReal());


//        LOG.info("Angle: {}. Length: {}", angle, length);

        Complex energies[] = new Complex[5];

        double fraction = computeEnergyDissipation(angle, 0, Math.PI / 16);

//        LOG.info("Fraction1: {}", fraction);

        energies[0] = ComplexUtils.polar2Complex(fraction * length, fixAngle(angle - Math.PI / 2));
        energies[4] = ComplexUtils.polar2Complex(fraction * length, fixAngle(angle + Math.PI / 2));

        fraction = computeEnergyDissipation(angle, Math.PI / 16, Math.PI / 4);

//        LOG.info("Fraction2: {}", fraction);

        energies[1] = ComplexUtils.polar2Complex(fraction * length, fixAngle(angle - Math.PI / 4));
        energies[3] = ComplexUtils.polar2Complex(fraction * length, fixAngle(angle + Math.PI / 4));

        fraction = computeEnergyDissipation(angle, Math.PI / 4, Math.PI / 2); // angle - Math.PI / 4 + Math.PI / 8, angle + Math.PI / 4 - Math.PI / 8);

//        LOG.info("Fraction3: {}", fraction);

        energies[2] = ComplexUtils.polar2Complex(2 * fraction * length, fixAngle(angle));


        return energies;
    }


    static Complex[] distributeEnergy(Complex flow, TileType neighbours[]) {
        double length = flow.abs();
        double angle = fixAngle(flow.getArgument()); // Math.atan(pFlow.getImaginary() / pFlow.getReal());


        FlowDirection direction = getDirection(flow.getArgument());
        int cell = direction.ordinal();

        Complex temp[] = distributeEnergy(flow);
        Complex distributed[] = new Complex[neighbours.length];
        System.arraycopy(temp, 0, distributed, 0, temp.length);
        distributed[distributed.length - 3] = Complex.ZERO;
        distributed[distributed.length - 2] = Complex.ZERO;
        distributed[distributed.length - 1] = Complex.ZERO;

        FlowDirection startDirection = getDirection(direction, -2);
        int shift = startDirection.ordinal();

        double result[] = new double[distributed.length + 1];
        for (int i = 0; i < result.length; ++i) {
            result[i] = 0;
        }

        for (int i = 0; i < neighbours.length; ++i) {
            int temp2 = i - shift;
            if (temp2 >= 8) {
                temp2 -= 8;
            } else if (temp2 < 0) {
                temp2 += 8;
            }

            switch (neighbours[i]) {

                // TODO Set back to something else than 0

                case OUTSIDE_SCREEN:

//                    result[result.length - 1] = result[result.length - 1] + -distributed[temp2].abs();
                    result[i] = Double.NaN;
                    break;

                case OUTSIDE_CHARACTER:
                    result[i] = distributed[temp2].abs() * 0.5;
                    break;

                case OPEN:
                    result[i] = distributed[temp2].abs();
                    break;

            }

        }

        // TODO

        return null;

    }


    public static double computeEnergyDissipation(double pAngle, double pStart, double pStop) {
//        double energyStart = calculateEnergy(pAngle, pStart);
//        double energyStop = calculateEnergy(pAngle, pStop);
//
//        LOG.info("Energy start: {}. Energy stop: {}", energyStart, energyStop);
//
//        return energyStop - energyStart;

        return (pStop - pStart) / Math.PI;

    }

    private static double fixAngle(double pAngle) {
        if (pAngle < 0) {
            return pAngle + 2 * Math.PI;
        }
        if (pAngle >= 2 * Math.PI) {
            return pAngle - 2 * Math.PI;
        }
        return pAngle;
    }


    public static double calculateEnergy(double pAngle, double pNewAngle) {

        double shift = Math.PI / 2 - pAngle;

        double angle = pAngle + shift;
        double newAngle = pNewAngle + shift;

        angle = fixAngle(angle);
        newAngle = fixAngle(newAngle);

        if (newAngle > Math.PI) {
            return 0;
        }
        double area = Math.abs(angle - newAngle);
        return area / Math.PI;
    }


    private static void addNotProcessedNeighbours(int pRow, int pColumn, boolean pProcessedPoints[][], Queue<Integer> pRowQueue,
                                                  Queue<Integer> pColumnQueue) {
        if (pRow > 0) {
            if (pColumn > 0 && !pProcessedPoints[pRow - 1][pColumn - 1]) {
                pRowQueue.add(pRow - 1);
                pColumnQueue.add(pColumn - 1);
            }
            if (pColumn < pProcessedPoints.length - 1 && !pProcessedPoints[pRow - 1][pColumn + 1]) {
                pRowQueue.add(pRow - 1);
                pColumnQueue.add(pColumn - 1);
            }
            pRowQueue.add(pRow - 1);
            pColumnQueue.add(pColumn);
        }
        if (pRow < pProcessedPoints.length - 1) {
            if (pColumn > 0 && !pProcessedPoints[pRow + 1][pColumn - 1]) {
                pRowQueue.add(pRow + 1);
                pColumnQueue.add(pColumn - 1);
            }
            if (pColumn < pProcessedPoints.length - 1 && !pProcessedPoints[pRow + 1][pColumn + 1]) {
                pRowQueue.add(pRow + 1);
                pColumnQueue.add(pColumn - 1);
            }
            pRowQueue.add(pRow + 1);
            pColumnQueue.add(pColumn);
        }
        if (pColumn > 0) {
            pRowQueue.add(pRow);
            pColumnQueue.add(pColumn - 1);
        }
        if (pColumn < pProcessedPoints.length - 1) {
            pRowQueue.add(pRow);
            pColumnQueue.add(pColumn + 1);
        }

    }


    private static FlowDirection getDirection(FlowDirection direction, int shift) {
        int temp = direction.ordinal() + shift;
        if (temp < 0) {
            temp += FlowDirection.values().length;
        } else if (temp >= FlowDirection.values().length) {
            temp -= FlowDirection.values().length;
        }
        return FlowDirection.values()[temp];
    }


    static FlowDirection getDirection(double pAngle) {
        pAngle = fixAngle(pAngle);
        if (pAngle < Math.PI / 8 || pAngle >= 7 / 4 * Math.PI) {
            return FlowDirection.EAST;
        }

        int directionCounter = 1;
        for (double d = Math.PI / 4; d < 2 * Math.PI - Math.PI / 8; d += Math.PI / 4) {
            if (pAngle < d + Math.PI / 8) {
                return FlowDirection.values()[directionCounter];
            }
            ++directionCounter;
        }
        throw new IllegalArgumentException("Unexpected angle: " + pAngle);
    }



}
