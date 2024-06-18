package polytech.diploma.models.streams;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "streams")
@Data
@NoArgsConstructor
public class LiveStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String rtspUrl;

    @Column(unique = true)
    private String dashUrl;

    private String streamName;

    private Boolean record;

    private String groupName;
}
