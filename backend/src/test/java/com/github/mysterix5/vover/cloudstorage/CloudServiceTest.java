package com.github.mysterix5.vover.cloudstorage;

import com.github.mysterix5.vover.model.AudioResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Map;

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

    AudioInputStream getAudioInputStreamFromFilePath(String filePath) throws UnsupportedAudioFileException, IOException {

        File file = new File(filePath);
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(decodedFormat, in);
    }

    private AudioInputStream getFromFilePath(String path) throws IOException, UnsupportedAudioFileException {
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = fileInputStream.readAllBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        AudioFileFormat baseFormat = AudioSystem.getAudioFileFormat(byteArrayInputStream);
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getFormat().getSampleRate(),
                16,
                baseFormat.getFormat().getChannels(),
                baseFormat.getFormat().getChannels() * 2,
                baseFormat.getFormat().getSampleRate(),
                false);
        return new AudioInputStream(byteArrayInputStream, baseFormat.getFormat(), baseFormat.getFrameLength());
    }

    @Test
    void loadMultipleAudioFromCloudAndMergeMp3() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        CloudRepository cloudRepository = Mockito.mock(CloudRepository.class);
        CloudService cloudService = new CloudService(cloudRepository);

        String filePath1 = "/Users/lukas/Downloads/bei.mp3";
        String filePath2 = "/Users/lukas/Downloads/euch.mp3";
        String filePath3 = "/Users/lukas/Downloads/euch.mp3";
//        File file1 = new File(filePath1);
//        AudioFileFormat format1 = AudioSystem.getAudioFileFormat(file1);
//        FileInputStream fileInputStream1 = new FileInputStream(file1);
//        System.out.println(format1);
//        System.out.println(format1.getByteLength() / format1.getFrameLength());
//        System.out.println(format1.getFrameLength());

//        AudioInputStream din1 = getAudioInputStreamFromFilePath(filePath1);
//        AudioInputStream din2 = getAudioInputStreamFromFilePath(filePath2);
//        AudioInputStream din3 = getAudioInputStreamFromFilePath(filePath3);

        AudioInputStream din1 = getFromFilePath(filePath1);
        AudioInputStream din2 = getFromFilePath(filePath2);
        AudioInputStream din3 = getFromFilePath(filePath3);

        System.out.println(din1.getFormat());
        System.out.println(din1.getFrameLength());

        List<AudioInputStream> audioInputStreams = new ArrayList<>();
        audioInputStreams.add(din1);
        audioInputStreams.add(din2);
        audioInputStreams.add(din3);

//        rawplay2(din1.getFormat(), audioInputStreams);

        AudioInputStream a1 = mergeAudioStreams(audioInputStreams);
        System.out.println(a1.getFormat());

        File fileOut = new File("out.mp3");
//        AudioSystem.write(a1, AudioFileFormat.Type., byteArrayOutputStream);

        AudioFileFormat baseFormat = AudioSystem.getAudioFileFormat(a1);
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getFormat().getSampleRate(),
                16,
                baseFormat.getFormat().getChannels(),
                baseFormat.getFormat().getChannels() * 2,
                baseFormat.getFormat().getSampleRate(),
                false);
        rawplay2(decodedFormat, List.of(a1));


//        new AudioInputStream(line.);
//        File outputFile = new File("output.mp3");
//        AudioSystem.write(din, AudioFileFormat.Type., outputFile);


//        File file2 = new File(filePath2);
//        AudioFileFormat format2 = AudioSystem.getAudioFileFormat(file2);
//        FileInputStream fileInputStream2 = new FileInputStream(file2);
//        System.out.println(format2);
//        System.out.println(format2.getByteLength()/format2.getFrameLength());
//        System.out.println(format2.getFrameLength());
//
//        AudioInputStream audioInputStream1 = new AudioInputStream(fileInputStream1, format1.getFormat(), format1.getFrameLength());
//        AudioInputStream audioInputStream2 = new AudioInputStream(fileInputStream2, format2.getFormat(), format2.getFrameLength());
////
//        var lengthAll = format1.getFrameLength() + format2.getFrameLength();
//        System.out.println(lengthAll);
//
//        SequenceInputStream sequenceInputStream = new SequenceInputStream(audioInputStream1, audioInputStream2);
//
//        AudioInputStream appended =
//                new AudioInputStream(
//                        sequenceInputStream,
//                        format1.getFormat(),
//                        lengthAll);
//
//
//        AudioFormat baseFormat = appended.getFormat();
//        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                baseFormat.getSampleRate(),
//                16,
//                baseFormat.getChannels(),
//                baseFormat.getChannels() * 2,
//                baseFormat.getSampleRate(),
//                false);
//
//        rawplay(decodedFormat, appended);
//
//        AudioFormat audioFileFormatAppended = appended.getFormat();
//
//        System.out.println(audioFileFormatAppended);
//        System.out.println(audioFileFormatAppended.getFrameSize());
//        System.out.println(audioFileFormatAppended.getFrameRate());
//        System.out.println(audioFileFormatAppended.getEncoding());
//
////        File outputFile = new File("output.mp3");
////        AudioSystem.write(appended, format1.getType(), outputFile);
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        AudioSystem.write(appended, format1.getType(), byteArrayOutputStream);


//        AudioInputStream in = AudioSystem.getAudioInputStream(bytes);

//        AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
//        System.out.println(audioFileFormat);

    }

    private AudioInputStream mergeAudioStreams(List<AudioInputStream> audioInputStreams) throws IOException, UnsupportedAudioFileException {
        byte[] data = new byte[512];
        byte[] buf = new byte[512];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int length = 0;
        for (AudioInputStream audioInputStream : audioInputStreams) {

            length += audioInputStream.getFrameLength();

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

    private SourceDataLine rawplay2(AudioFormat targetFormat, List<AudioInputStream> audioInputStreams) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        DataLine.Info info;
        info = new DataLine.Info(TargetDataLine.class, targetFormat);
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
//        targetDataLine.read()
        SourceDataLine line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            for (AudioInputStream audioInputStream : audioInputStreams) {
                int nBytesRead = 0, nBytesWritten = 0;
                while (nBytesRead != -1) {
                    nBytesRead = audioInputStream.read(data, 0, data.length);
                    // DecodedMpegAudioInputStream properties
//                    if (audioInputStream instanceof javazoom.spi.PropertiesContainer) {
//                        Map properties = ((javazoom.spi.PropertiesContainer) audioInputStream).properties();
//                        float[] equalizer = (float[]) properties.get("mp3.equalizer");
//                        equalizer[0] = (float) 0.5;
//                        equalizer[31] = (float) 0.25;
//
//                        String eq = "";
//                        for (int i = 0; i < equalizer.length; i++) {
//                            eq = eq + equalizer[i] + "   ";
//                        }
//                        System.out.println("Equal: " + eq);
//                    }
                    if (nBytesRead != -1) {
                        nBytesWritten = line.write(data, 0, nBytesRead);
                    }
                }
                audioInputStream.close();
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
        }
        return line;
    }

    private static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null) {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1) {
                nBytesRead = din.read(data, 0, data.length);
                // DecodedMpegAudioInputStream properties
                if (din instanceof javazoom.spi.PropertiesContainer) {
                    Map properties = ((javazoom.spi.PropertiesContainer) din).properties();
                    float[] equalizer = (float[]) properties.get("mp3.equalizer");
                    equalizer[0] = (float) 0.5;
                    equalizer[31] = (float) 0.25;

                    String eq = "";
                    for (int i = 0; i < equalizer.length; i++) {
                        eq = eq + equalizer[i] + "   ";
                    }
                    System.out.println("Equal: " + eq);
                }
                if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }
}