package com.kjipo.raster.flow;

import org.apache.commons.math3.complex.Complex;


public class Source {
    private int x;
    private int y;
//    private final int flow;
//    private final Map<FlowDirections, Complex> flowDirections = new HashMap<>();
    private Complex flow;



    public Source(int pX, int pY, Complex pFlow) { // int pFlow, Map<FlowDirections, Complex> pFlowDirections) {
        x = pX;
        y = pY;
//        flow = pFlow;
//        flowDirections.putAll(pFlowDirections);
        flow = pFlow;
    }

    public Source(Source source) {
        this.x = source.x;
        this.y = source.y;
        this.flow = new Complex(source.flow.getReal(), source.getFlow().getImaginary());
    }


    public int getRow() {
        return x;
    }

    public void setRow(int x) {
        this.x = x;
    }

    public int getColumn() {
        return y;
    }

    public void setColumn(int y) {
        this.y = y;
    }

//    public Map<FlowDirections, Complex> getFlowDirections() {
//        return flowDirections;
//    }

    public Complex getFlow() {
        return flow;
    }

    @Override
    public String toString() {
        return "Source x: " +x +" y: " +y +". Flow: " +flow;
    }

}
