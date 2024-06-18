package polytech.diploma.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveResponseDTO {
    private Long id;
    private String dashUrl;
    private String rtspUrl;
    private String streamName;
    private Boolean record;
    private String groupName;
}
