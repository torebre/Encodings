package com.kjipo.representation;


import java.io.PrintStream;
import java.io.Serializable;
import java.util.stream.IntStream;


public class EncodedKanji implements Serializable {
    private final boolean image[][];


    public EncodedKanji(boolean image[][]) {
        this.image = image;
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

    public void printKanji() {
        printKanji(System.out);
    }

}
