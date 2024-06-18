package polytech.diploma.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polytech.diploma.dtos.LiveRequestDTO;
import polytech.diploma.dtos.LiveResponseDTO;
import polytech.diploma.dtos.RequestRecordDTO;
import polytech.diploma.services.stream.LiveService;
import polytech.diploma.services.stream.VideoService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/streamPage/")
@CrossOrigin
@RequiredArgsConstructor
public class StreamPageController {

    private final LiveService liveService;
    private final VideoService videoService;

    @PostMapping("controlStream/startStream")
    public ResponseEntity<LiveResponseDTO> startStream(@RequestBody LiveRequestDTO liveRequestDTO) {
        LiveResponseDTO liveResponseDTO = liveService.startStreaming(liveRequestDTO);

        return ResponseEntity.ok(liveResponseDTO);
    }

    @DeleteMapping("controlStream/{streamId}/delete")
    public ResponseEntity<Void> deleteStream(@PathVariable Long streamId) {
        liveService.deleteStreamById(streamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("streams")
    public ResponseEntity<Set<LiveResponseDTO>> getStreams(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Set<LiveResponseDTO> streams = liveService.getStreamsByPage(pageable);

        return ResponseEntity.ok().body(streams);
    }

    @PostMapping("streamsByGroup")
    public ResponseEntity<Set<LiveResponseDTO>> getStreamsByGroup(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            @RequestParam(name= "group") String group)
    {
        Pageable pageable = PageRequest.of(page, size);
        Set<LiveResponseDTO> streams = liveService.getStreamsByGroup(group, pageable);

        return ResponseEntity.ok().body(streams);
    }


    @PostMapping(value = "get-video", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ByteArrayResource> getVideo(
            @RequestBody @Valid RequestRecordDTO videoRequest) throws IOException {

        ByteArrayResource video = new ByteArrayResource(videoService.mergeVideos(videoRequest));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", "attachment; filename=my_video.mp4");

        // Отправка видеоролика в ответ
        return ResponseEntity.ok()
                .headers(headers)
                .body(video);
    }
}
