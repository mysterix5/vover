package com.github.mysterix5.capstone.textHandling;

import com.github.mysterix5.capstone.model.Availability;
import com.github.mysterix5.capstone.model.WordResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextService {
    private final WordsMongoRepository wordsRepository;
    public List<WordResponseDTO> onSubmittedText(String text) {
        List<String> wordList = splitText(text);

        return createResponses(wordList);
    }

    private List<String> splitText(String text){
        return Arrays.stream(text.split(" ")).toList();
    }

    private List<WordResponseDTO> createResponses(List<String> wordList){
        List<WordResponseDTO> textResponseInfo= new ArrayList<>();
        for(String word: wordList){
            WordResponseDTO wordResponse = new WordResponseDTO(word.toLowerCase());
            if(wordValidCheck(wordResponse)){
                getDbInformation(wordResponse);
            }
            textResponseInfo.add(wordResponse);
        }
        return textResponseInfo;
    }

    private void getDbInformation(WordResponseDTO wordResponse){
        if(wordsRepository.existsByWord(wordResponse.getWord())){
            wordResponse.setAvailability(Availability.PUBLIC);
        }else{
            wordResponse.setAvailability(Availability.ABSENT);
        }
    }


    // TODO grow with functionality
    private boolean wordValidCheck(WordResponseDTO responseWord){
        List<String> forbiddenChars = List.of("/", "%");
        for(String c: forbiddenChars){
            if(responseWord.getWord().contains(c)){
                responseWord.setAvailability(Availability.INVALID);
                return false;
            }
        }
        return true;
    }

}