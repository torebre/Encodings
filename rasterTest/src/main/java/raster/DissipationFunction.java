package raster;


@FunctionalInterface
public interface DissipationFunction {

    double apply(double observation, double value);

}
