package visualization;

public interface RasterRun<T extends CellType> {

    boolean[][] getRawInput();

    boolean hasNext();

    int getWidth();

    int getHeight();

    T getCell(int row, int column);




}
