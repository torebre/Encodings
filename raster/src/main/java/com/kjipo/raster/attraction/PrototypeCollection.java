package com.kjipo.raster.attraction;


import com.kjipo.prototype.Prototype;
import com.kjipo.raster.segment.Segment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PrototypeCollection implements Prototype {
    private final Collection<Prototype> prototypes;


    public PrototypeCollection(Collection<Prototype> prototypes) {
        this.prototypes = prototypes;
    }

    @Override
    public List<Segment> getSegments() {
        return prototypes.stream()
                .flatMap(prototype -> prototype.getSegments().stream())
                .collect(Collectors.toList());
    }
}
