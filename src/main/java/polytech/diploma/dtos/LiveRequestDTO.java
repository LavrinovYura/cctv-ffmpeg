package polytech.diploma.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LiveRequestDTO {
    private String rtspUrl;
    private String streamName;
    private Boolean record;
    private String groupName;
}
