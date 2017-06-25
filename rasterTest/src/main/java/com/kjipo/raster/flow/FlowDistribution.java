package com.kjipo.raster.flow;


import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.kjipo.raster.DissipationFunction;
import com.kjipo.raster.FlowDirection;
import com.kjipo.raster.TileType;

import java.util.Arrays;
import java.util.stream.Collectors;


public class FlowDistribution {
    private static double OUTSIDE_CHARACTER_PENALTY = 0.1;
    public static final double TRIANGLE_BASE = Math.PI;


    private static final Logger LOGGER = LoggerFactory.getLogger(FlowDistribution.class);


    public static void applyObstructions(double unobstructedFlow[], TileType neighbourTiles[]) {
        for (int i = 0; i < neighbourTiles.length; ++i) {
            switch (neighbourTiles[i]) {
                case OUTSIDE_SCREEN:
                    unobstructedFlow[i] = 0;
                    break;

                case OUTSIDE_CHARACTER:
                    unobstructedFlow[i] *= OUTSIDE_CHARACTER_PENALTY;
                    break;

                case OPEN:
                default:
                    break;
            }
        }
    }

    public static double[] distributeFlowWithNoObstructions(
            double flow, double angleInRadians, DissipationFunction distributionFunction) {
        return Doubles.toArray(Arrays.stream(FlowDirection.values())
                .map(FlowDirection::getAngleInRadians)
                .map(input -> distributionFunction.apply(angleInRadians, input))
                .map(input -> input * flow)
                .collect(Collectors.toList()));
    }

    public static double[] distributeFlow(double flow, double angleInRadians) {
        return distributeFlowWithNoObstructions(flow, angleInRadians, createTriangularDissipationFunction(TRIANGLE_BASE));
    }

    public static double[] distributeFlow(double flow, double angleInRadians, DissipationFunction dissipationFunction) {
        return distributeFlowWithNoObstructions(flow, angleInRadians, dissipationFunction);
    }

    public static DissipationFunction createTriangularDissipationFunction(double triangleBase) {
        return ((observation, value) -> triangularDissipationFunction(observation, value, triangleBase));
    }

    public static double triangularDissipationFunction(double observation, double value, double triangleBase) {
        LOGGER.debug("Observation: {}. Value: {}", observation, value);

        observation += 2 * Math.PI;
        value += 2 * Math.PI;

        double temp = observation - value;

        if (temp > Math.PI) {
            value += 2 * Math.PI;
        } else if (temp < -Math.PI) {
            value -= 2 * Math.PI;
        }

//        temp = observation - stop;
//        if(temp > Math.PI) {
//            stop += 2 * Math.PI;
//        }
//        else if(temp < -Math.PI) {
//            stop -= 2 * Math.PI;
//        }

        double start = value - Math.PI / 8;
        double stop = value + Math.PI / 8;

//        observation += 2 * Math.PI;
//        start += 2 * Math.PI;
//        stop += 2 * Math.PI;

        LOGGER.debug("Observation: {}. Range: {}, {}", observation, start, stop);

        double sum = computeIntegralForTriangularDissipationFunction(observation, start, stop, triangleBase);

        LOGGER.debug("Sum: {}", sum);

        return sum;
    }

    public static double computeIntegralForTriangularDissipationFunction(
            double observation, double start, double stop, double triangleBase) {
        if (start == stop) {
            return 0;
        }

//        double temp = observation - start;
//        if(temp > Math.PI) {
//            start += 2 * Math.PI;
//        }
//        else if(temp < -Math.PI) {
//            start -= 2 * Math.PI;
//        }
//
//        temp = observation - stop;
//        if(temp > Math.PI) {
//            stop += 2 * Math.PI;
//        }
//        else if(temp < -Math.PI) {
//            stop -= 2 * Math.PI;
//        }


        if (start <= observation && stop >= observation) {
            double volumeOfStartTriangle = computeTriangleVolume(start - observation, triangleBase);
            double volumeOfStopTriangle = computeTriangleVolume(stop - observation, triangleBase);

            LOGGER.debug("Volume of start triangle: {}. Volume of stop triangle: {}",
                    volumeOfStartTriangle, volumeOfStopTriangle);

            return 1 - volumeOfStartTriangle - volumeOfStopTriangle;
        } else if (start <= observation && stop <= observation) {
            double volumeOfStartTriangle = computeTriangleVolume(start - observation, triangleBase);
            double volumeOfStopTriangle = computeTriangleVolume(stop - observation, triangleBase);
            return Math.abs(volumeOfStartTriangle - volumeOfStopTriangle);
        } else {
            double volumeOfStartTriangle = computeTriangleVolume(start - observation, triangleBase);
            double volumeOfStopTriangle = computeTriangleVolume(stop - observation, triangleBase);
            return Math.abs(volumeOfStartTriangle - volumeOfStopTriangle);
        }
    }

    public static double computeTriangleVolume(double distanceFromObservation, double triangleBase) {
        distanceFromObservation = Math.abs(distanceFromObservation);
        if (distanceFromObservation >= triangleBase) {
            return 0;
        }
        return (1 / (2 * triangleBase)) * (1 - distanceFromObservation / triangleBase) * (triangleBase - distanceFromObservation);
    }

    private static Double constrictToTwoPiRange(Double angle) {
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle <= -2 * Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

}
