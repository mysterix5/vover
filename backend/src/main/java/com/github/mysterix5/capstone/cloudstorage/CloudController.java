package com.github.mysterix5.capstone.cloudstorage;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cloud")
public class CloudController {
    private final CloudService cloudService;

    @GetMapping("/save")
    public void saveFileToCloud() throws IOException {
        String localFilePath = "got_easy.wav";
        String cloudFilePath = "test1.wav";
        cloudService.saveFile(localFilePath, cloudFilePath);
    }
}
