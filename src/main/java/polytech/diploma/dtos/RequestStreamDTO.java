package polytech.diploma.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestStreamDTO {
    private String rtspUrl;
    private String streamName;
}
