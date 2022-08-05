package com.kjipo.raster.attraction;

import com.kjipo.representation.prototype.AngleLine;

public interface AngleLineMoveOperation {

    void apply(AngleLine angleLine);

    void applyStretching(AngleLine angleLine);

    void applyRotation(AngleLine angleLine);

    double getRotation();


}
