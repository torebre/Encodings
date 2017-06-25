package com.kjipo.parser;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Parsers {
    public static final Path FONT_FILE_LOCATION = Paths.get("/home/student/workspace/testEncodings/encodings/src/main/resources", "font/kochi-mincho-subst.ttf");
    public static final Path EDICT_FILE_LOCATION = Paths.get("/home/student/edict/edict2");
    public static final Charset JAPANESE_CHARSET = Charset.forName("EUC_JP");

    private static final Logger logger = LoggerFactory.getLogger(Parsers.class);


    public static void main(String args[]) throws IOException, FontFormatException {
        InputStream fontStream = new FileInputStream(FONT_FILE_LOCATION.toFile());
        Font testFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        fontStream.close();
        InputStreamReader input = new InputStreamReader(
                new BufferedInputStream(new FileInputStream(EDICT_FILE_LOCATION.toFile())), JAPANESE_CHARSET);
        BufferedReader reader = new BufferedReader(input);
        String line;

        int counter = 0;

        while ((line = reader.readLine()) != null) {

            String character = line.substring(0, line.indexOf('/'));

            System.out.println("Character " + counter++ + ": " + character);

        }


    }


}
