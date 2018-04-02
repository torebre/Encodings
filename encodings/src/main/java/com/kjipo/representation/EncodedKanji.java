package com.kjipo.representation;


import java.io.PrintStream;
import java.io.Serializable;
import java.util.stream.IntStream;


public class EncodedKanji implements Serializable {
    private final String character;
    private final boolean image[][];
    private final int unicode;


//    public EncodedKanji(String character, boolean[][] image) {
//        Preconditions.checkNotNull(character);
//        this.character = character;
//        this.image = image;
//        this.unicode = Character.codePointAt(new char[] {character}, 0);
//    }

    public EncodedKanji(String character, boolean[][] image, int unicode) {
        this.character = character;
        this.image = image;
        this.unicode = unicode;
    }

    public EncodedKanji(boolean[][] image, int unicode) {
        this.character = new String(Character.toChars(unicode));
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


    public String getCharacter() {
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

    public int getUnicode() {
        return unicode;
    }
}
