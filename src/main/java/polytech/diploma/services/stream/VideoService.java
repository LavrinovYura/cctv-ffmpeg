package polytech.diploma.services.stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import polytech.diploma.dtos.RequestRecordDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoService {

    @Value("${VideoService.videos.path}")
    private String videosPath;

    @Value("${VideoService.output.path}")
    private String outputPath;

    public byte[] mergeVideos(RequestRecordDTO videoRequest) throws IOException {
        String cameraName = videoRequest.getCameraName();
        int year = Integer.parseInt(videoRequest.getYear());
        int month = Integer.parseInt(videoRequest.getMonth());
        int day = Integer.parseInt(videoRequest.getDay());
        String time = videoRequest.getTime();
        int duration = Integer.parseInt(videoRequest.getDuration());

        // Директория для текущей камеры
        String cameraDirectoryPath = outputPath + cameraName + "/";
        Files.createDirectories(Paths.get(cameraDirectoryPath));

        String listFilePath = cameraDirectoryPath + "mylist.txt";
        String videoPath = cameraDirectoryPath + "output.mp4";

        // Удаляем существующие файлы
        Files.deleteIfExists(Paths.get(videoPath));
        Files.deleteIfExists(Paths.get(listFilePath));

        // Логирование в файл
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(cameraDirectoryPath + "logfile.txt"));
             BufferedWriter listWriter = new BufferedWriter(new FileWriter(listFilePath))) {

            LocalDateTime dateTime = LocalDateTime.of(year, month, day,
                    Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]));

            for (int i = 0; i < duration; i++) {
                String path = String.format("%s/%02d/%02d/%02d_%02d.mp4", year, month, day,
                        dateTime.getHour(), dateTime.getMinute());

                Path videoFilePath = Paths.get(videosPath, cameraName, path);
                if (Files.exists(videoFilePath)) {
                    listWriter.write("file '" + videoFilePath + "'\n");
                }

                dateTime = dateTime.plusMinutes(1);

                // Проверяем переход месяца и года
                if (dateTime.getDayOfMonth() != day) {
                    day = dateTime.getDayOfMonth();
                    month = dateTime.getMonthValue();
                    year = dateTime.getYear();
                }
            }

            logWriter.write("File list creation completed.\n");
        } catch (IOException e) {
            throw new MyIOException("Ошибка создания списка файлов или логирования: " + e.getMessage(), e);
        }

        executeFfmpegCommand(listFilePath, videoPath);

        // Возвращаем результат в виде массива байтов
        Path outputVideoPath = Paths.get(videoPath);
        byte[] videoBytes = Files.readAllBytes(outputVideoPath);

        // Удаляем временные файлы и директорию
        Files.deleteIfExists(Paths.get(listFilePath));
        Files.deleteIfExists(outputVideoPath);
        Files.deleteIfExists(Paths.get(cameraDirectoryPath + "logfile.txt"));
        Files.deleteIfExists(Paths.get(cameraDirectoryPath));

        return videoBytes;
    }


    private void executeFfmpegCommand(String listFilePath, String videoPath) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-i");
        command.add(listFilePath);
        command.add("-c");
        command.add("copy");
        command.add(videoPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        try {
            int concatExitCode = process.waitFor();
            if (concatExitCode != 0) {
                throw new MyIOException("Ошибка выполнения команды ffmpeg. Код завершения: " + concatExitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MyIOException("Процесс был прерван: " + e.getMessage(), e);
        }
    }

    public static class MyIOException extends IOException {
        public MyIOException(String message) {
            super(message);
        }

        public MyIOException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
