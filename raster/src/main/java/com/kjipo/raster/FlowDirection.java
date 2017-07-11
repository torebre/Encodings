package com.kjipo.raster;


public enum FlowDirection {
    EAST(0, 1, 0),
    NORTH_EAST(-1, 1, Math.PI / 4),
    NORTH(-1, 0, Math.PI / 2),
    NORTH_WEST(-1, -1, 3 * Math.PI / 4),
    WEST(0, -1, Math.PI),
    SOUTH_WEST(1, -1, 5 * Math.PI / 4),
    SOUTH(1, 0, 3 * Math.PI / 2),
    SOUTH_EAST(1, 1, 7 * Math.PI / 4);

    private final int rowShift;
    private final int columnShift;
    private final double angleInRadians;


    FlowDirection(int rowShift, int columnShift, double angleInRadians) {
        this.rowShift = rowShift;
        this.columnShift = columnShift;
        this.angleInRadians = angleInRadians;
    }

    public int getRowShift() {
        return rowShift;
    }

    public int getColumnShift() {
        return columnShift;
    }

    public double getAngleInRadians() {
        return angleInRadians;
    }

}
