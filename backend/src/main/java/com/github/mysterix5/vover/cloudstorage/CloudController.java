package com.github.mysterix5.vover.cloudstorage;

import com.github.mysterix5.vover.model.AudioResponseDTO;
import com.github.mysterix5.vover.model.WordResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/stream")
public class CloudController {

    private final CloudRepository cloudRepository;

    @GetMapping("/test")
    public void getTestMp3(HttpServletResponse httpResponse) throws UnsupportedAudioFileException, IOException {
        String filePath1 = "/Users/lukas/Downloads/bei.mp3";
        String filePath2 = "/Users/lukas/Downloads/euch.mp3";
        String filePath3 = "/Users/lukas/Downloads/euch.mp3";

        AudioInputStream din1 = getFromFilePath(filePath1);
        AudioInputStream din2 = getFromFilePath(filePath2);
        AudioInputStream din3 = getFromFilePath(filePath3);

        List<AudioInputStream> audioInputStreams = new ArrayList<>();
        audioInputStreams.add(din1);
        audioInputStreams.add(din2);
        audioInputStreams.add(din3);

        AudioInputStream a1 = mergeAudioStreams(audioInputStreams);

//        httpResponse.setContentLength(audioResponseDTO.getContentLength());
        httpResponse.setContentType("audio/mp3");
        httpResponse.getOutputStream().write(a1.readAllBytes());
    }
    @GetMapping
    public void getTestMp3fromCloud(HttpServletResponse httpResponse) throws UnsupportedAudioFileException, IOException {
        String filePath1 = "bei.mp3";
        String filePath2 = "euch.mp3";
        List<String> urlList = new ArrayList<>();
        urlList.add(filePath1);
        urlList.add(filePath2);


//        var first = cloudRepository.find(filePath1);
//        var second = cloudRepository.find(filePath2);
//
//        AudioInputStream din1 = getFromFilePath2(first);
//        AudioInputStream din2 = getFromFilePath2(second);
//
//        List<AudioInputStream> audioInputStreams = new ArrayList<>();
//        audioInputStreams.add(din1);
//        audioInputStreams.add(din2);
//
//        AudioInputStream a1 = mergeAudioStreams(audioInputStreams);

        AudioInputStream mergedAudio = getAndMergeAudioFromCloud(urlList);

//        httpResponse.setContentLength(audioResponseDTO.getContentLength());
        httpResponse.setContentType("audio/mp3");
        httpResponse.getOutputStream().write(mergedAudio.readAllBytes());
    }

    private AudioInputStream getAndMergeAudioFromCloud(List<String> urls) throws UnsupportedAudioFileException, IOException {
        List<AudioInputStream> audioInputStreams = urls.parallelStream().map((url)->{
            try {
                return cloudRepository.find(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(bytes -> {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            AudioFileFormat baseFormat = null;
            try {
                baseFormat = AudioSystem.getAudioFileFormat(byteArrayInputStream);
            } catch (UnsupportedAudioFileException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new AudioInputStream(byteArrayInputStream, baseFormat.getFormat(), baseFormat.getFrameLength());
        }).toList();

        return mergeAudioStreams(audioInputStreams);
    }

    private AudioInputStream getFromFilePath2(byte[] bytes) throws IOException, UnsupportedAudioFileException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        AudioFileFormat baseFormat = AudioSystem.getAudioFileFormat(byteArrayInputStream);
        return new AudioInputStream(byteArrayInputStream, baseFormat.getFormat(), baseFormat.getFrameLength());
    }
    private AudioInputStream getFromFilePath(String path) throws IOException, UnsupportedAudioFileException {
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = fileInputStream.readAllBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        AudioFileFormat baseFormat = AudioSystem.getAudioFileFormat(byteArrayInputStream);
        return new AudioInputStream(byteArrayInputStream, baseFormat.getFormat(), baseFormat.getFrameLength());
    }

    private AudioInputStream mergeAudioStreams(List<AudioInputStream> audioInputStreams) throws IOException, UnsupportedAudioFileException {
        byte[] data = new byte[512];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for (AudioInputStream audioInputStream : audioInputStreams) {
            int nBytesRead = 0;
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(data, 0, data.length);
                if (nBytesRead != -1) {
                    byteArrayOutputStream.write(data, 0, nBytesRead);
                }
            }
            audioInputStream.close();
        }
        var format = audioInputStreams.get(0).getFormat();
        return new AudioInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                format,
                byteArrayOutputStream.size());
    }
}
