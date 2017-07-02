package visualization;

public interface RasterRun<T extends CellType> {

    boolean[][] getRawInput();

    boolean hasNext();

    int getColumns();

    int getRows();

    T getCell(int row, int column);

    void next();




}
