package com.github.mysterix5.vover.static_tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringOperations {

    public static boolean isWord(String word){
        String wordRegex = "^[\\p{L}\\p{M}]{1,30}$";
        return word.matches(wordRegex);
    }

    public static List<String> splitText(String text){
        List<String> wordList = new ArrayList<>();
        text = text.replaceAll("^\\s+", "");
        text = text.replaceAll("\\s+$", "");
        if(text.equals("")){
            return wordList;
        }
        wordList = Arrays.stream(text.split("\\s+")).toList();

        return wordList;
    }

    public static boolean isUsername(String username) {
        String usernameRegex = "^(?=.{3,20}$)(?![_-])(?!.*[_-]{2})[a-zA-Z0-9-_]+(?<![_-])$";
        return username.matches(usernameRegex);
    }
}
