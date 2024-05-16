package polytech.diploma.services.stream;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import polytech.diploma.dtos.RequestStreamDTO;
import polytech.diploma.models.streams.LiveStream;
import polytech.diploma.repositories.StreamRepository;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LiveService {

    @Value("${LiveService.hls.host}")
    private String hlsUrl;

    @Value("${LiveService.hls.path}")
    private String hlsPath;

    private final StreamRepository streamRepository;

    public String startStreaming(RequestStreamDTO requestStreamDTO) {
        String rtspUrl = requestStreamDTO.getRtspUrl();
        Optional<LiveStream> stream = streamRepository.findByRtspUrl(rtspUrl);

        if (stream.isPresent()) {
            return stream.get().getHlsUrl();
        }

        String hlsStreamUrl = hlsUrl + requestStreamDTO.getStreamName() + "/" + requestStreamDTO.getStreamName() + ".m3u8";

        // Создаем директорию для HLS стрима
        createDirectory(hlsPath + requestStreamDTO.getStreamName());

        // Запускаем асинхронную задачу
        CountDownLatch latch = new CountDownLatch(1);
        startFFmpegAsync(requestStreamDTO, latch);

        try {
            // Ожидаем, пока FFmpeg начнет стриминг
            latch.await(4, TimeUnit.SECONDS); // Таймаут ожидания 10 секунд
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFmpeg process was interrupted", e);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep was interrupted", e);
        }

        LiveStream createLiveStream = new LiveStream();
        createLiveStream.setHlsUrl(hlsStreamUrl);
        createLiveStream.setRtspUrl(rtspUrl);

        streamRepository.save(createLiveStream);

        return hlsStreamUrl;
    }

    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + path);
            }
        }
    }

    public void startFFmpegAsync(RequestStreamDTO requestStreamDTO, CountDownLatch latch) {
        CompletableFuture.runAsync(() -> {
            FrameProducer producer = new FrameProducer() {
                private long frameCounter = 0;

                @Override
                public List<Stream> produceStreams() {
                    return Collections.singletonList(new Stream()
                            .setType(Stream.Type.VIDEO)
                            .setTimebase(1000L)
                            .setWidth(1280)
                            .setHeight(720)
                    );
                }

                @Override
                public Frame produce() {
                    BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D graphics = image.createGraphics();

                    // Заливка фона черным цветом
                    graphics.setPaint(Color.BLACK);
                    graphics.fillRect(0, 0, 1280, 720);

                    // Отображение текущего времени
                    graphics.setPaint(Color.WHITE);
                    graphics.setFont(new Font("Arial", Font.BOLD, 48));
                    graphics.drawString(java.time.LocalTime.now().toString(), 100, 100);

                    // Отображение названия стрима
                    graphics.drawString(requestStreamDTO.getStreamName(), 100, 200);

                    long pts = frameCounter * 1000 / 30; // Frame PTS in Stream Timebase
                    Frame videoFrame = Frame.createVideoFrame(0, pts, image);
                    frameCounter++;

                    return videoFrame;
                }
            };

            FFmpeg ffmpeg = FFmpeg.atPath()
                    .addInput(FrameInput.withProducer(producer))
                    .addOutput(UrlOutput.toUrl(hlsPath + requestStreamDTO.getStreamName() + "/" + requestStreamDTO.getStreamName() + ".m3u8")
                            .setFormat("hls")
                            .addArguments("-hls_time", "1.3")
                            .addArguments("-hls_list_size", "3")
                            .addArguments("-hls_flags", "delete_segments")
                            .addArguments("-c:v", "libx264")
                            .addArguments("-preset", "ultrafast")
                            .addArguments("-tune", "zerolatency")
                            .addArguments("-crf", "23")
                            .addArguments("-profile:v", "main") // Изменение профиля на main
                            .addArguments("-pix_fmt", "yuv420p") // Изменение формата пикселей
                            .addArguments("-c:a", "aac") // Добавление пустого аудио
                            .addArguments("-b:a", "128k")
                            .setFrameRate(30)
                            .disableStream(StreamType.AUDIO)
                            .disableStream(StreamType.SUBTITLE)
                            .disableStream(StreamType.DATA)

                    )
                    .setProgressListener(progress -> {
                        if (progress.getFrame() > 0) {
                            latch.countDown();
                        }
                    });

            ffmpeg.execute();
        });
    }
}
