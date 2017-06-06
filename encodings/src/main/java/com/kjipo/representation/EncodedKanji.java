package com.kjipo.representation;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class EncodedKanji {
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

    public String getKanjiString() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output, true);
        printStream.flush();
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }

}
