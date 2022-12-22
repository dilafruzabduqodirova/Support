package infinity.learningcenter.service.Impl;

import infinity.learningcenter.dto.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekTableDto {
    String week_names ;
    List<RoomDto>roomDtos;
}
