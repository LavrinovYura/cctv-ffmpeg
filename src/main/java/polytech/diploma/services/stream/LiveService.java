package polytech.diploma.services.stream;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffmpeg.Frame;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import polytech.diploma.dtos.LiveRequestDTO;
import polytech.diploma.dtos.LiveResponseDTO;
import polytech.diploma.exceptions.ResourceNotFoundException;
import polytech.diploma.mappers.LiveStreamMapper;
import polytech.diploma.models.streams.LiveStream;
import polytech.diploma.repositories.StreamRepository;

import javax.persistence.EntityNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LiveService {

    @Value("${LiveService.dash.host}")
    private String dashUrl;

    @Value("${LiveService.dash.path}")
    private String dashPath;

    @Value("${LiveService.record.path}")
    private String recordPath;

    private final Map<Long, FFmpegResultFuture> runningStreams = new ConcurrentHashMap<>();

    private final StreamRepository streamRepository;
    private final LiveStreamMapper liveStreamMapper;

    public Set<LiveResponseDTO> getStreamsByPage(Pageable pageable) {
        Page<LiveStream> liveStreamPage = streamRepository.findAll(pageable);

        if (liveStreamPage.isEmpty()) {
            throw new ResourceNotFoundException("No live streams found");
        }

        return liveStreamMapper.liveStreamToDTOSet(liveStreamPage);
    }

    public Set<LiveResponseDTO> getStreamsByGroup(String group, Pageable pageable) {
        Page<LiveStream> liveStreamPage = streamRepository.findAllByGroupName(group, pageable);

        if (liveStreamPage.isEmpty()) {
            throw new ResourceNotFoundException("No live streams found");
        }

        return liveStreamMapper.liveStreamToDTOSet(liveStreamPage);
    }

    public LiveResponseDTO startStreaming(LiveRequestDTO liveRequestDTO) {
        String rtspUrl = liveRequestDTO.getRtspUrl();
        Optional<LiveStream> stream = streamRepository.findByRtspUrl(rtspUrl);

        if (stream.isPresent()) {
            return liveStreamMapper.liveStreamToDTO(stream.get());
        }

        LiveStream liveStream = liveStreamMapper.liveStreamFromDTO(liveRequestDTO);

        String hlsStreamUrl = dashUrl + liveStream.getStreamName() + "/" + liveStream.getStreamName() + ".mpd";

        // Создаем директорию для HLS стрима
        createDirectory(dashPath + liveStream.getStreamName());

        // Запускаем асинхронную задачу
        CountDownLatch latch = new CountDownLatch(1);
        FFmpegResultFuture streamFuture = startFFmpegAsync(liveStream, latch, liveRequestDTO.getRecord());

        try {
            // Ожидаем, пока FFmpeg начнет стриминг
            latch.await(4, TimeUnit.SECONDS);
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

        liveStream.setDashUrl(hlsStreamUrl);

        long streamId = streamRepository.save(liveStream).getId();
        runningStreams.put(streamId, streamFuture);

        return liveStreamMapper.liveStreamToDTO(liveStream);
    }

    public void deleteStreamById(Long streamId) {
        streamRepository.findById(streamId)
                .orElseThrow(() -> new EntityNotFoundException("Stream not found with id " + streamId));

        FFmpegResultFuture future = runningStreams.get(streamId);
        if (future != null) {
            future.graceStop();  // Или forceStop() если необходимо
            runningStreams.remove(streamId);
        }

        streamRepository.deleteById(streamId);
    }

    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + path);
            }
        }
    }

    private String createRecordingPath(String basePath, String streamName, LocalDate date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy\\MM\\dd");
        System.out.println(date.format(dateFormatter) + "FORMATER");
        return basePath + streamName + "\\" + date.format(dateFormatter);
    }


    public FFmpegResultFuture startFFmpegAsync(LiveStream liveStream, CountDownLatch latch, Boolean record) {

        FrameProducer producer = new FrameProducer() {
            private long nextVideoTimecode = 0;
            private final long frameRate = 165;
            private final long timebase = 1000;

            @Override
            public List<Stream> produceStreams() {
                return Collections.singletonList(new Stream()
                        .setType(Stream.Type.VIDEO)
                        .setTimebase(timebase)
                        .setWidth(854)
                        .setHeight(480)
                );
            }

            @Override
            public Frame produce() {
                BufferedImage image = new BufferedImage(854, 480, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphics = image.createGraphics();

                // Заливка фона черным цветом
                graphics.setPaint(Color.BLACK);
                graphics.fillRect(0, 0, 854, 480);

                // Отображение текущего времени
                graphics.setPaint(Color.WHITE);
                graphics.setFont(new Font("Arial", Font.BOLD, 48));
                graphics.drawString(java.time.LocalTime.now().toString(), 100, 100);

                // Отображение названия стрима
                graphics.drawString(liveStream.getStreamName(), 100, 200);

                // Frame PTS in Stream Timebase
                Frame videoFrame = Frame.createVideoFrame(0, nextVideoTimecode, image);
                nextVideoTimecode += timebase / frameRate;
                return videoFrame;
            }
        };

        FFmpeg ffmpeg = FFmpeg.atPath()
                //.addInput(FrameInput.withProducer(producer))
                .addInput(
                        UrlInput.fromUrl(liveStream.getRtspUrl())
                                .setFormat("rtsp")
                                .addArguments("-fflags", "nobuffer")
                                .addArguments("-flags", "low_delay")
                                .addArguments("-strict", "experimental")
                                .addArguments("-fflags", "flush_packets")
                                .addArguments("-flush_packets", "1")
                                .addArguments("-rtsp_transport", "udp")
                                .addArguments("-analyzeduration", "0")
                )
                .addOutput(UrlOutput.toUrl(dashPath + liveStream.getStreamName() + "/" + liveStream.getStreamName() + ".mpd")
                        .addArguments("-r", "30") // Указываем частоту кадров для входного потока
                        .addArguments("-force_key_frames", "expr:gte(t,n_forced*2)")
                        .setFormat("dash")
                        .addArguments("-strict", "experimental")
                        .addArguments("-ldash", "1")
                        .addArguments("-use_template", "1")
                        .addArguments("-use_timeline", "1")
                        .addArguments("-index_correction", "1")
                        .addArguments("-preset", "ultrafast") // Ускоренное кодирование
                        .addArguments("-crf", "23") // Постоянный уровень качества
                        .addArguments("-profile:v", "main") // Профиль видео
                        .addArguments("-tune", "zerolatency") // Настройка для минимальной задержки
                        .addArguments("-seg_duration", "2") // Длительность сегмента 1 секунда
                        .addArguments("-frag_duration", "1") // Длительность фрагмента 1 секунда
                        .addArguments("-utc_timing_url", "https://time.akamai.com/?iso") // URL для синхронизации времени
                        .addArguments("-frag_type", "duration") // Тип фрагмента
                        .addArguments("-target_latency", "2") // Целевая задержка 2 секунды
                        .addArguments("-window_size", "15") // Размер окна
                        .addArguments("-extra_window_size", "15") // Дополнительный размер окна
                        .addArguments("-streaming", "1") // Режим потоковой передачи
                        .addArguments("-remove_at_exit", "1") // Удаление фрагментов при выходе
                        .addArguments("-maxrate", "1557k") // Максимальная скорость потока
                        .addArguments("-bufsize", "1557k") // Размер буфера
                        .addArguments("-b:v", "1024k") // Битрейт видео
                        .addArguments("-c:v", "libx264") // Кодек видео
                        .disableStream(StreamType.SUBTITLE) // Отключение субтитров
                        .disableStream(StreamType.DATA) // Отключение данных
                )
                .setProgressListener(progress -> {
                    if (progress.getFrame() > 0) {
                        latch.countDown();
                    }
                });


        if (record) {
            LocalDate[] currentDate = {LocalDate.now()};
            String recordingPath = createRecordingPath(recordPath, liveStream.getStreamName(), currentDate[0]);
            createDirectory(recordingPath);
            String outputFilePattern = recordPath + liveStream.getStreamName() + "\\%Y\\%m\\%d\\" + "%H_%M.mp4";

            ffmpeg.addOutput(UrlOutput.toUrl(outputFilePattern)
                            .setFormat("segment")
                            .addArguments("-strftime", "1")
                            .addArguments("-strftime_mkdir", "1")
                            .addArguments("-segment_time", "60") // 1 минута
                            .addArguments("-reset_timestamps", "1")
                            .addArguments("-c:v", "libx264")
                            .addArguments("-preset", "ultrafast")
                            .addArguments("-tune", "zerolatency")
                            .addArguments("-crf", "23")
                            .addArguments("-pix_fmt", "yuv420p")
                            .addArguments("-c:a", "aac")
                            .addArguments("-b:a", "128k")
                            .setFrameRate(15))
                    .setProgressListener(progress -> {
                        LocalDate now = LocalDate.now();
                        if (!now.equals(currentDate[0])) {
                            currentDate[0] = now;
                            String newRecordingPath = createRecordingPath(recordPath, liveStream.getStreamName(), currentDate[0]);
                            createDirectory(newRecordingPath);
                        }
                    });
        }
        return ffmpeg.executeAsync();
    }
}

