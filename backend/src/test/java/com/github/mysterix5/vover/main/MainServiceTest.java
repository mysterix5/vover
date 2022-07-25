package com.github.mysterix5.vover.main;

import com.github.mysterix5.vover.cloudStorage.CloudService;
import com.github.mysterix5.vover.model.other.Accessibility;
import com.github.mysterix5.vover.model.other.Availability;
import com.github.mysterix5.vover.model.text.TextResponseDTO;
import com.github.mysterix5.vover.model.word.WordDbEntity;
import com.github.mysterix5.vover.model.word.WordDbResponseDTO;
import com.github.mysterix5.vover.model.word.WordResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO tests on text valid checks etc. should be added here regularly as functionality grows
class MainServiceTest {

    @Test
    void testOnSubmittedTextSimpleValid() {
        String testString = "bester test";

        WordsMongoRepository mockedWordRepo = Mockito.mock(WordsMongoRepository.class);

        WordDbEntity wordDbEntity1 = WordDbEntity.builder().id("id1").word("bester").creator("creator1").tag("tag1").cloudFileName("bester.mp3").accessibility(Accessibility.PUBLIC).build();
        WordDbEntity wordDbEntity2 = WordDbEntity.builder().id("id2").word("test").creator("creator2").tag("tag2").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        when(mockedWordRepo.findByWordIn(new HashSet<>(List.of("bester", "test"))))
                .thenReturn(List.of(
                        wordDbEntity1,
                        wordDbEntity2
                ));
        CloudService mockedCloudService = Mockito.mock(CloudService.class);
        MainService mainService = new MainService(mockedWordRepo, mockedCloudService);

        TextResponseDTO response = mainService.onSubmittedText(testString, "user");

        TextResponseDTO expected = new TextResponseDTO(
                List.of(new WordResponseDTO("bester", Availability.PUBLIC), new WordResponseDTO("test", Availability.PUBLIC)),
                Map.of("bester", List.of(new WordDbResponseDTO(wordDbEntity1)),
                        "test", List.of(new WordDbResponseDTO(wordDbEntity2))));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void testOnSubmittedTextWithInvalidAndAbsent() {
        String testString = "beste/r test ever% wirklich";

        WordDbEntity wordDbEntity1 = WordDbEntity.builder().id("id1").word("test").creator("creator1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        WordsMongoRepository mockedWordRepo = Mockito.mock(WordsMongoRepository.class);
        when(mockedWordRepo.findByWordIn(new HashSet<>(List.of("test", "wirklich"))))
                .thenReturn(List.of(
                        wordDbEntity1
                ));
        CloudService mockedCloudService = Mockito.mock(CloudService.class);
        MainService mainService = new MainService(mockedWordRepo, mockedCloudService);

        var response = mainService.onSubmittedText(testString, "user");

        var expected = new TextResponseDTO(
                List.of(
                        new WordResponseDTO("beste/r", Availability.INVALID),
                        new WordResponseDTO("test", Availability.PUBLIC),
                        new WordResponseDTO("ever%", Availability.INVALID),
                        new WordResponseDTO("wirklich", Availability.NOT_AVAILABLE)
                ),
                Map.of(
                        "test", List.of(new WordDbResponseDTO(wordDbEntity1))
                ));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void loadWavFromCloudAndMerge() throws IOException {
        WordDbEntity wordDbEntity1 = WordDbEntity.builder().id("id1").word("test").creator("creator1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        WordDbEntity wordDbEntity2 = WordDbEntity.builder().id("id2").word("eins").creator("creator2").tag("tag2").cloudFileName("eins.mp3").accessibility(Accessibility.PUBLIC).build();

        WordsMongoRepository mockedWordRepo = Mockito.mock(WordsMongoRepository.class);
        when(mockedWordRepo.findAllById(List.of("id1", "id2"))).thenReturn(List.of(wordDbEntity1, wordDbEntity2));

        CloudService mockedCloudService = Mockito.mock(CloudService.class);
        MainService mainService = new MainService(mockedWordRepo, mockedCloudService);

        mainService.getMergedAudio(List.of("id1", "id2"));

        verify(mockedCloudService).loadMultipleAudioFromCloudAndMerge(List.of("test.mp3", "eins.mp3"));
    }
}