package visualization;

public abstract class AbstractCell implements CellType {
    protected final int row;
    protected final int column;


    protected AbstractCell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
