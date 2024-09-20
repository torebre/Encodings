package com.kjipo.experiments

import com.kjipo.representation.Matrix

data class VisualizationData(val imageMatrix: Matrix<Boolean>, val pointTypeImage: Matrix<Array<PointType>>)