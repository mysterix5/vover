package com.github.mysterix5.vover.records;

import com.github.mysterix5.vover.audio_processing.AudioProcessingService;
import com.github.mysterix5.vover.cloud_storage.CloudService;
import com.github.mysterix5.vover.model.record.Accessibility;
import com.github.mysterix5.vover.model.record.RecordManagementDTO;
import com.github.mysterix5.vover.model.record.RecordPage;
import com.github.mysterix5.vover.model.record.RecordDbEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.notNull;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {
    @Mock
    RecordMongoRepository mockedRecordRepo;
    @Mock
    CloudService mockedCloudService;
    AudioProcessingService audioProcessingService;
    RecordService recordService;

    @BeforeEach
    void setupUserService() {
        audioProcessingService = new AudioProcessingService();
        recordService = new RecordService(mockedRecordRepo, mockedCloudService, audioProcessingService);
    }

    // TODO only single and valid records allowed
    @Test
    void addRecordToDb() throws IOException {

        UUID myUuid = new UUID(10, 10);

        String word = "record";
        String creator = "creator";
        String tag = "tag";
        Accessibility accessibility = Accessibility.PUBLIC;
        RecordDbEntity recordDbEntity = new RecordDbEntity(word, creator, tag, word + "-" + creator + "-" + tag + "-" + accessibility + "-" + myUuid + ".mp3");

        File publicFile = new File("src/test/resources/cloud_storage/public.mp3");
        InputStream publicStream = new FileInputStream(publicFile);

        Mockito.when(mockedRecordRepo.save(notNull())).thenReturn(recordDbEntity);
        try (MockedStatic<UUID> mb = Mockito.mockStatic(UUID.class)) {
            mb.when(UUID::randomUUID).thenReturn(myUuid);

            recordService.addRecordToDb(word, creator, tag, accessibility.toString(), publicStream);

            try {
                Mockito.verify(mockedCloudService).save(notNull(), notNull());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Test
    void getRecordPage() {
        int page = 0;
        int size = 2;

        String searchTerm = "";
        String username = "1";

        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("creator1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        RecordDbEntity recordDbEntity2 = RecordDbEntity.builder().id("id2").word("eins").creator("creator2").tag("tag2").cloudFileName("eins.mp3").accessibility(Accessibility.PUBLIC).build();

        Pageable paging = PageRequest.of(page, size);
        Page<RecordDbEntity> resultPage = new PageImpl<>(List.of(recordDbEntity1, recordDbEntity2), paging, 2);

        Mockito.when(mockedRecordRepo.findByCreatorAndWordLike(username, searchTerm, paging)).thenReturn(resultPage);

        RecordPage recordPage = recordService.getRecordPage(username, page, size, searchTerm);

        RecordPage expected = RecordPage.builder()
                .page(page)
                .size(size)
                .noPages(1)
                .searchTerm(searchTerm)
                .records(List.of(new RecordManagementDTO(recordDbEntity1), new RecordManagementDTO(recordDbEntity2)))
                .accessibilityChoices(Accessibility.values()).build();

        assertThat(recordPage).isEqualTo(expected);
    }

    @Test
    void getAudioSuccess() {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        var obFile = new File("src/test/resources/cloud_storage/eins.mp3");
        try (var obStream = new FileInputStream(obFile)) {
            byte[] bytes = obStream.readAllBytes();

            Mockito.when(mockedCloudService.find(recordDbEntity1.getCloudFileName())).thenReturn(bytes);

            byte[] actual = recordService.getAudio(recordDbEntity1.getId(), username);

            assertThat(actual).isEqualTo(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAudioFailBecausePrincipalDoesNotOwnThisRecord() {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user2";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.getAudio(recordDbEntity1.getId(), username))
                .withMessage("The audio file you requested is not yours. Don't try to hack me! :(");
    }

    @Test
    void getAudioFailBecauseCloudThrowsError() throws IOException {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        Mockito.when(mockedCloudService.find(recordDbEntity1.getCloudFileName())).thenThrow(new IOException());

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.getAudio(recordDbEntity1.getId(), username))
                .withMessage("Your audio could not be found in the cloud");
    }

    @Test
    void deleteRecordSuccess() throws IOException {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        recordService.deleteRecord(recordDbEntity1.getId(), username);
        Mockito.verify(mockedCloudService).delete(recordDbEntity1.getCloudFileName());
    }

    @Test
    void deleteRecordFailBecausePrincipalDoesNotOwnThisRecord() {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user2";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.deleteRecord(recordDbEntity1.getId(), username))
                .withMessage("The audio file you requested is not yours. Don't try to hack me! :(");
    }

    @Test
    void deleteRecordFailBecauseCloudThrowsError() throws IOException {
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordDbEntity1.getId())).thenReturn(Optional.of(recordDbEntity1));

        Mockito.doThrow(new IOException()).when(mockedCloudService).delete(recordDbEntity1.getCloudFileName());

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.deleteRecord(recordDbEntity1.getId(), username))
                .withMessage("Deleting your audio file failed, did nothing");
    }

    @Test
    void changeRecordMetadata() {
        String oldCloudFileName = "test.mp3";
        RecordManagementDTO recordManagementDTO = new RecordManagementDTO("id1", "test1", "tag1", Accessibility.PUBLIC);
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName(oldCloudFileName).accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordManagementDTO.getId())).thenReturn(Optional.of(recordDbEntity1));

        try (MockedStatic<UUID> mb = Mockito.mockStatic(UUID.class)) {
            UUID myUuid = new UUID(10, 10);
            mb.when(UUID::randomUUID).thenReturn(myUuid);
            String newCloudFileName = recordManagementDTO.getWord() + "-" +
                    username + "-" +
                    recordManagementDTO.getTag() + "-" +
                    recordManagementDTO.getAccessibility().toString() + "-" +
                    myUuid +
                    ".mp3";

            recordService.changeRecordMetadata(recordManagementDTO, username);

            Mockito.verify(mockedCloudService).move(oldCloudFileName, newCloudFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void changeRecordMetadataFailBecausePrincipalDoesNotOwnThisRecord() {
        RecordManagementDTO recordManagementDTO = new RecordManagementDTO("id1", "test1", "tag1", Accessibility.PUBLIC);
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user2";

        Mockito.when(mockedRecordRepo.findById(recordManagementDTO.getId())).thenReturn(Optional.of(recordDbEntity1));

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.changeRecordMetadata(recordManagementDTO, username))
                .withMessage("The record you requested to change is not yours. Don't try to hack me! :(");
    }

    @Test
    void changeRecordMetadataFailBecauseNoChanges() {
        RecordManagementDTO recordManagementDTO = new RecordManagementDTO("id1", "test", "tag1", Accessibility.PUBLIC);
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordManagementDTO.getId())).thenReturn(Optional.of(recordDbEntity1));

        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.changeRecordMetadata(recordManagementDTO, username))
                .withMessage("Nothing was changed");
    }

    @Test
    void changeRecordMetadataFailBecauseCloudThrowsIOError() {
        RecordManagementDTO recordManagementDTO = new RecordManagementDTO("id1", "test1", "tag1", Accessibility.PUBLIC);
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id("id1").word("test").creator("user1").tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();
        String username = "user1";

        Mockito.when(mockedRecordRepo.findById(recordManagementDTO.getId())).thenReturn(Optional.of(recordDbEntity1));

        try (MockedStatic<UUID> mb = Mockito.mockStatic(UUID.class)) {
            UUID myUuid = new UUID(10, 10);
            mb.when(UUID::randomUUID).thenReturn(myUuid);
            String newCloudFileName = recordManagementDTO.getWord() + "-" +
                    username + "-" +
                    recordManagementDTO.getTag() + "-" +
                    recordManagementDTO.getAccessibility().toString() + "-" +
                    myUuid +
                    ".mp3";

            Mockito.doThrow(new IOException()).when(mockedCloudService).move(recordDbEntity1.getCloudFileName(), newCloudFileName);

            Assertions.assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> recordService.changeRecordMetadata(recordManagementDTO, username))
                    .withMessage("Something went wrong changing your record metadata");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateAudioFailsBecauseAudioIsNotFromUser() throws IOException {
        // Setup
        String username = "testuser";
        String usernameDb = "dbuser";
        String id = UUID.randomUUID().toString();
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id(id).word("test").creator(usernameDb).tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();

        Mockito.when(mockedRecordRepo.findById(id)).thenReturn(Optional.of(recordDbEntity1));

        File publicFile = new File("src/test/resources/cloud_storage/public.mp3");
        InputStream newAudio = new FileInputStream(publicFile);

        // Exercise
        Assertions.assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> recordService.updateAudio(username, id, newAudio))
                .withMessage("The audio file you requested is not yours. Don't try to hack me! :(");
    }

    @Test
    void updateAudio() throws IOException {
        // Setup
        String username = "testuser";
        String id = UUID.randomUUID().toString();
        RecordDbEntity recordDbEntity1 = RecordDbEntity.builder().id(id).word("test").creator(username).tag("tag1").cloudFileName("test.mp3").accessibility(Accessibility.PUBLIC).build();

        Mockito.when(mockedRecordRepo.findById(id)).thenReturn(Optional.of(recordDbEntity1));

        File publicFile = new File("src/test/resources/cloud_storage/public.mp3");
        InputStream newAudio = new FileInputStream(publicFile);

        // Exercise
        recordService.updateAudio(username, id, newAudio);

        Mockito.verify(mockedCloudService).save(notNull(), notNull());
    }
}
