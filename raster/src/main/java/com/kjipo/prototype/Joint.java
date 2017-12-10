package com.kjipo.prototype;

import com.kjipo.raster.segment.Pair;

public class Joint {
    private Pair joint;

    public Joint(Pair joint) {
        this.joint = joint;
    }


    public Pair getJoint() {
        return joint;
    }

    public void setJoint(Pair joint) {
        this.joint = joint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Joint joint1 = (Joint) o;

        return joint != null ? joint.equals(joint1.joint) : joint1.joint == null;
    }

    @Override
    public int hashCode() {
        return joint != null ? joint.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Joint{" +
                "joint=" + joint +
                '}';
    }
}
