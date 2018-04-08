package com.kjipo.representation;


import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;


public class EncodedKanji implements Serializable {
    private final boolean image[][];
    private final int unicode;


    public EncodedKanji(boolean[][] image, int unicode) {
        this.image = image;
        this.unicode = unicode;
    }

    public boolean[][] getImage() {
        return image;
    }


    public void printKanji(PrintStream printStream) {
        IntStream.range(0, image.length).forEach(row -> {
            IntStream.range(0, image[0].length).forEach(column ->
                    printStream.print(image[row][column] ? "X" : " "));
            printStream.print("\n");
        });
    }

    public int getUnicode() {
        return unicode;
    }

    @Override
    public String toString() {
        return "EncodedKanji{" +
                "image=" + Arrays.toString(image) +
                ", unicode=" + unicode +
                '}';
    }
}
