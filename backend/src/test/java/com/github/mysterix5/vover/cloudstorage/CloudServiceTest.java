package com.github.mysterix5.vover.cloudstorage;

import com.github.mysterix5.vover.model.AudioResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CloudServiceTest {

    @Test
    void loadMultipleAudioFromCloudAndMerge() throws IOException {
        CloudRepository cloudRepository = Mockito.mock(CloudRepository.class);
        CloudService cloudService = new CloudService(cloudRepository);

        var obFile = new File("src/test/resources/com.github.mysterix5.vover.cloudstorage/ob.wav");
        var obStream = new FileInputStream(obFile);
        var dasFile = new File("src/test/resources/com.github.mysterix5.vover.cloudstorage/das.wav");
        var dasStream = new FileInputStream(dasFile);

        Mockito.when(cloudRepository.find("ob.wav")).thenReturn(obStream.readAllBytes());
        Mockito.when(cloudRepository.find("das.wav")).thenReturn(dasStream.readAllBytes());

        AudioResponseDTO returnVal = cloudService.loadMultipleAudioFromCloudAndMerge(List.of("ob.wav", "das.wav"));

        var obDasFile = new File("src/test/resources/com.github.mysterix5.vover.cloudstorage/obDas.wav");
        var obDasStream = new FileInputStream(obDasFile);
        var obDasBytes = obDasStream.readAllBytes();

        assertThat(returnVal.getContentLength()).isEqualTo(344064);
        assertThat(returnVal.getContentType()).isEqualTo("audio/x-wav");
        assertThat(returnVal.getData()).isEqualTo(obDasBytes);
    }
    @Test
    void loadMultipleAudioFromCloudAndMergeMp3() throws IOException, UnsupportedAudioFileException {
        CloudRepository cloudRepository = Mockito.mock(CloudRepository.class);
        CloudService cloudService = new CloudService(cloudRepository);

        String filePath = "/Users/lukas/Downloads/bei.mp3";
        File file = new File(filePath);
//        var fileInputStream = new FileInputStream(new File(filePath));
//        var bytes = fileInputStream.readAllBytes();
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//
        AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
        System.out.println(format);
//        AudioInputStream in = AudioSystem.getAudioInputStream(bytes);

//        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
//        System.out.println(audioFileFormat);

    }
}