package polytech.diploma.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import polytech.diploma.dtos.LiveRequestDTO;
import polytech.diploma.dtos.LiveResponseDTO;
import polytech.diploma.models.streams.LiveStream;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface LiveStreamMapper {
    LiveStream liveStreamFromDTO(LiveRequestDTO liveRequestDTO);

    @Named(value= "liveStreamToDTO")
    LiveResponseDTO liveStreamToDTO(LiveStream liveStream);

    @IterableMapping(qualifiedByName = "liveStreamToDTO")
    Set<LiveResponseDTO> liveStreamToDTOSet(Page<LiveStream> liveStreams);

}
