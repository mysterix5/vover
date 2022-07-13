package com.github.mysterix5.capstone.cloudstorage;

import java.util.List;

import com.github.mysterix5.capstone.model.AudioResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class CloudServiceTest {

    @Test
    void loadListFromCloudAndMerge(){
        byte[] byteArr_ob = "ob audio file".getBytes();
        byte[] byteArr_das = "das audio file".getBytes();
        CloudRepository mockedCloudRepository = Mockito.mock(CloudRepository.class);
        when(mockedCloudRepository.find("ob.wav")).thenReturn(byteArr_ob);
        when(mockedCloudRepository.find("das.wav")).thenReturn(byteArr_das);
        CloudService cloudService = new CloudService(mockedCloudRepository);

        try {
            var r = cloudService.loadListFromCloudAndMerge(List.of("ob", "das"));
            assertThat(r).isEqualTo("new AudioResponseDTO()");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    }