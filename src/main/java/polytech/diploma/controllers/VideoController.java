package polytech.diploma.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polytech.diploma.dtos.RequestStreamDTO;
import polytech.diploma.services.stream.LiveService;

@RestController
@RequestMapping("/video")
@CrossOrigin
@RequiredArgsConstructor
public class VideoController {

    private final LiveService liveService;

    @PostMapping("/start")
    public ResponseEntity<String> startStream(@RequestBody RequestStreamDTO requestStreamDTO) {
        String hlsUrl = liveService.startStreaming(requestStreamDTO);

        return ResponseEntity.ok(hlsUrl);
    }

}
