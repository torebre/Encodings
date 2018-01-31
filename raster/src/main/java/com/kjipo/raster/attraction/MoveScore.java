package com.kjipo.raster.attraction;

public class MoveScore {
    private final MoveOperation moveOperation;
    private final int score;

    public MoveScore(MoveOperation moveOperation, int score) {
        this.moveOperation = moveOperation;
        this.score = score;
    }

    public MoveOperation getMoveOperation() {
        return moveOperation;
    }

    public int getScore() {
        return score;
    }
}
