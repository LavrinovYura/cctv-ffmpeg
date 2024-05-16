//package polytech.diploma.services.stream;
//
//import com.github.kokorin.jaffree.StreamType;
//import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
//import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
//import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
//import com.github.kokorin.jaffree.ffmpeg.UrlInput;
//import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import polytech.diploma.dtos.RequestStreamDTO;
//import polytech.diploma.models.streams.Stream;
//import polytech.diploma.repositories.StreamRepository;
//
//import java.io.File;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//public class LiveService {
//
//    @Value("${LiveService.hls.host}")
//    private String hlsUrl;
//
//    @Value("${LiveService.hls.path}")
//    private String hlsPath;
//
//    private final StreamRepository streamRepository;
//
//    public String startStreaming(RequestStreamDTO requestStreamDTO) {
//        String rtspUrl = requestStreamDTO.getRtspUrl();
//        Optional<Stream> stream = streamRepository.findByRtspUrl(rtspUrl);
//
//        if (stream.isPresent()) {
//            return stream.get().getHlsUrl();
//        }
//
//        String hlsStreamUrl = hlsUrl  + requestStreamDTO.getStreamName() + "/" + requestStreamDTO.getStreamName() + ".m3u8";
//
//        // Создаем директорию для HLS стрима
//        createDirectory(hlsPath + requestStreamDTO.getStreamName());
//
//        // Запускаем асинхронную задачу
//        CountDownLatch latch = new CountDownLatch(1);
//        startFFmpegAsync(requestStreamDTO, latch);
//
//        try {
//            // Ожидаем, пока FFmpeg начнет стриминг
//            latch.await(10, TimeUnit.SECONDS); // Таймаут ожидания 10 секунд
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("FFmpeg process was interrupted", e);
//        }
//
//        Stream createStream = new Stream();
//        createStream.setHlsUrl(hlsStreamUrl);
//        createStream.setRtspUrl(rtspUrl);
//
//        streamRepository.save(createStream);
//
//        return hlsStreamUrl;
//    }
//
//    private void createDirectory(String path) {
//        File directory = new File(path);
//        if (!directory.exists()) {
//            if (!directory.mkdirs()) {
//                throw new RuntimeException("Failed to create directory: " + path);
//            }
//        }
//    }
//
//    public void startFFmpegAsync(RequestStreamDTO requestStreamDTO, CountDownLatch latch) {
//        CompletableFuture.runAsync(() -> {
//            FFmpeg.atPath()
//                    .addInput(
//                            UrlInput.fromUrl(requestStreamDTO.getRtspUrl())
//                                    .setFormat("rtsp")
//                                    .addArguments("-fflags", "nobuffer")
//                                    .addArguments("-flags", "low_delay")
//                                    .addArguments("-strict", "experimental")
//                                    .addArguments("-fflags", "flush_packets")
//                                    .addArguments("-flush_packets", "1")
//                                    .addArguments("-rtsp_transport", "udp")
//                                    .addArguments("-analyzeduration", "0")
//                    )
//                    .setFilter(StreamType.VIDEO, "scale=1280:720")
//                    .addOutput(
//                            UrlOutput.toUrl(hlsPath + requestStreamDTO.getStreamName() + "/" + requestStreamDTO.getStreamName() + ".m3u8")
//                                    .setFormat("hls")
//                                    .addArguments("-x264-params", "keyint=60")
//                                    .addArguments("-hls_time", "1.3")  // Длина сегментов HLS
//                                    .addArguments("-hls_list_size", "3")  // Количество сегментов в плейлисте
//                                    .addArguments("-hls_flags", "delete_segments")  // Удаление старых сегментов
//                                    .addArguments("-c:v", "libx264")
//                                    .addArguments("-preset", "ultrafast")
//                                    .addArguments("-tune", "zerolatency")
//                                    .addArguments("-crf", "23")
//                                    .addArguments("-c:a", "aac")
//                                    .addArguments("-b:a", "128k")
//                                    .setFrameRate(30)
//                    )
//                    .setProgressListener(progress -> {
//                        if (progress.getFrame() > 0) {
//                            latch.countDown();
//                        }
//                    })
//                    .execute();
//        });
//    }
//}
