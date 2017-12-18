package com.kjipo.raster.segment;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SegmentImpl implements Segment {
    private List<Pair> pairs;


    public SegmentImpl() {

    }


    public SegmentImpl(List<Pair> pairs) {
        if (pairs.isEmpty()) {
            throw new IllegalArgumentException("List of pairs cannot be empty");
        }
        this.pairs = pairs;
    }


    @Override
    public List<Pair> getPairs() {
        return pairs;
    }

    @Override
    public String toString() {
        return "SegmentImpl{" +
                "pairs=" + pairs +
                '}';
    }
}
