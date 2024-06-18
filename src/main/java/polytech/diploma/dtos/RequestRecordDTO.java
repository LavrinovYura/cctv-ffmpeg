package polytech.diploma.dtos;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class RequestRecordDTO {

    @NotBlank(message = "Camera number is required")
    private String cameraName;

    @NotBlank(message = "Year is required")
    @Pattern(regexp = "\\d{4}", message = "Year should be in the format 'yyyy'")
    private String year;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "\\d{1,2}", message = "Month should be in the format 'MM'")
    @Min(value = 1, message = "Month should be between 01 and 12")
    @Max(value = 12, message = "Month should be between 01 and 12")
    private String month;

    @NotBlank(message = "Day is required")
    @Pattern(regexp = "\\d{1,2}", message = "Day should be in the format 'dd'")
    @Min(value = 1, message = "Day should be between 01 and 31")
    @Max(value = 31, message = "Day should be between 01 and 31")
    private String day;

    @NotBlank(message = "Time is required")
    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$", message = "Time should be in the format 'HH:mm'")
    private String time;

    @NotBlank(message = "Duration is required")
    @Min(value = 1, message = "Duration should not be less than 1")
    @Max(value = 60, message = "Duration should not be greater than 60")
    private String duration;

}
