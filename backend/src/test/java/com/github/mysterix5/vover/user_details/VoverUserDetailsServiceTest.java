package com.github.mysterix5.vover.user_details;

import com.github.mysterix5.vover.history.HistoryService;
import com.github.mysterix5.vover.model.record.RecordDbEntity;
import com.github.mysterix5.vover.model.user_details.HistoryEntry;
import com.github.mysterix5.vover.model.user_details.VoverUserDetails;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class VoverUserDetailsServiceTest {

    @Test
    void addRequestToHistory() {
        VoverUserDetailsMongoRepository mockedUserDetailsRepository = Mockito.mock(VoverUserDetailsMongoRepository.class);
        HistoryService mockedHistoryService = Mockito.mock(HistoryService.class);
        VoverUserDetailsService voverUserDetailsService = new VoverUserDetailsService(mockedUserDetailsRepository, mockedHistoryService);

        String username = "user";
        List<RecordDbEntity> records = List.of(
                RecordDbEntity.builder().word("ein").id("id1").build(),
                RecordDbEntity.builder().word("kleiner").id("id2").build(),
                RecordDbEntity.builder().word("test").id("id3").build()
        );

        VoverUserDetails testUserDetails = new VoverUserDetails(username);

        var time = LocalDateTime.now();
        HistoryEntry historyEntry = new HistoryEntry(null, "ein kleiner test", List.of("id1", "id2", "id3"), time);

        Mockito.when(mockedUserDetailsRepository.findById(username)).thenReturn(Optional.of(testUserDetails));

        try (MockedStatic<LocalDateTime> mb = Mockito.mockStatic(LocalDateTime.class)) {
            mb.when(LocalDateTime::now).thenReturn(time).thenReturn(time);

            voverUserDetailsService.addRequestToHistory(username, records);

            Mockito.verify(mockedUserDetailsRepository).save(testUserDetails);
            Mockito.verify(mockedHistoryService).save(testUserDetails, historyEntry);
        }
    }

    @Test
    void getHistory() {
        VoverUserDetailsMongoRepository mockedUserDetailsRepository = Mockito.mock(VoverUserDetailsMongoRepository.class);
        HistoryService mockedHistoryService = Mockito.mock(HistoryService.class);
        VoverUserDetailsService voverUserDetailsService = new VoverUserDetailsService(mockedUserDetailsRepository, mockedHistoryService);

        String username = "user";
        List<HistoryEntry> history = List.of(new HistoryEntry(
                "testid",
                "ein kleiner test",
                List.of("1", "2", "3"),
                LocalDateTime.now())
        );

        VoverUserDetails testUserDetails = new VoverUserDetails(username);
        testUserDetails.setHistory(List.of("testid"));

        Mockito.when(mockedUserDetailsRepository.findById(username)).thenReturn(Optional.of(testUserDetails));
        Mockito.when(mockedHistoryService.getAllByIds(List.of("testid"))).thenReturn(history);

        List<HistoryEntry> actual = voverUserDetailsService.getHistory(username);

        assertThat(actual).isEqualTo(history);
    }
}