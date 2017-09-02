package com.kjipo.representation;


import com.google.common.base.Preconditions;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.stream.IntStream;


public class EncodedKanji implements Serializable {
    private final Character character;
    private final boolean image[][];


    public EncodedKanji(Character character, boolean[][] image) {
        Preconditions.checkNotNull(character);
        this.character = character;
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


    public Character getCharacter() {
        return character;
    }

    public void printKanji() {
        printKanji(System.out);
    }

    @Override
    public String toString() {
        return "EncodedKanji{" +
                "character=" + character +
                '}';
    }
}
