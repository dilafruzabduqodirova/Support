package infinity.learningcenter.service.Impl;

import infinity.learningcenter.dao.Group;
import infinity.learningcenter.dao.Room;
import infinity.learningcenter.dto.ResponseDto;
import infinity.learningcenter.dto.RoomDto;
import infinity.learningcenter.mapper.RoomMapper;
import infinity.learningcenter.repository.RoomRepository;
import infinity.learningcenter.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository repository;
    private final RoomMapper mapper;

    @Override
    public ResponseDto<String> add(RoomDto roomDto) {
        roomDto.setBooked(false);
        repository.save(mapper.toEntity(roomDto));
        return ResponseDto.<String>builder()
                .code(0)
                .success(true)
                .message("OK")
                .data("Successfully saved")
                .build();
    }

    @Override
    public ResponseDto<List<RoomDto>> getAll() {
        List<Room> rooms = repository.findAll();
        List<RoomDto> roomDtos = rooms.stream()
                .map(mapper::toDto).collect(Collectors.toList());
        return ResponseDto.<List<RoomDto>>builder()
                .code(0)
                .message("OK")
                .success(true)
                .data(roomDtos)
                .build();
    }

    @Override
    public ResponseDto<RoomDto> getById(Integer id) {
        Optional<Room> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseDto.<RoomDto>builder()
                    .code(-4)
                    .message("id not found")
                    .success(false)
                    .build();
        }
        return ResponseDto.<RoomDto>builder()
                .code(0)
                .message("OK")
                .success(true)
                .data(mapper.toDto(optional.get()))
                .build();
    }

    @Override
    public ResponseDto<RoomDto> getByName(String name) {
        Optional<Room> optional = repository.findFirstByName(name);
        if (optional.isEmpty()) {
            return ResponseDto.<RoomDto>builder()
                    .code(-3)
                    .message("Not found")
                    .success(false)
                    .build();
        }
        return ResponseDto.<RoomDto>builder()
                .code(0)
                .message("Ok")
                .success(true)
                .data(mapper.toDto(optional.get()))
                .build();
    }

    @Override
    public ResponseDto<List<RoomDto>> getByStatus(Boolean isEmpty) {
        List<Room> rooms = repository.getAllByStatus(isEmpty);
        List<RoomDto> roomDtoList = rooms.stream()
                .map(mapper::toDto).collect(Collectors.toList());
        return ResponseDto.<List<RoomDto>>builder()
                .code(0)
                .message("OK")
                .success(true)
                .data(roomDtoList)
                .build();
    }

    @Override
    public ResponseDto<RoomDto> update(RoomDto roomDto, Integer id) {
        if (repository.existsById(id)) {
            Room room = mapper.toEntity(roomDto);
            room.setId(id);
            repository.save(room);
            return ResponseDto.<RoomDto>builder()
                    .code(0)
                    .message("OK")
                    .success(true)
                    .data(mapper.toDto(room))
                    .build();
        }
        return ResponseDto.<RoomDto>builder()
                .code(-3)
                .message("id not found")
                .success(false)
                .build();
    }

    @Override
    public ResponseDto<String> deleteById(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseDto.<String>builder()
                    .code(0)
                    .message("OK")
                    .success(true)
                    .data("Successfully deleted")
                    .build();
        }
        return ResponseDto.<String>builder()
                .code(-3)
                .message("not found")
                .success(false)
                .data("id not found")
                .build();
    }

    public ResponseDto<List<WeekTableDto>> getOneWeek() {
        List<Room> rooms = repository.findAll();
        List<Room> roomsId1 = new ArrayList<>();
        List<Room> roomsId2 = new ArrayList<>();
        for (Room room : rooms) {
            Room roomId1 = map(room);
            roomId1.getGroupList().clear();
            Room roomId2 = map(room);
            roomId2.getGroupList().clear();
            for (Group group : room.getGroupList()) {
                if (group.getWeekId().equals(1)) {
                    roomId1.getGroupList().add(group);
                } else {
                    roomId2.getGroupList().add(group);
                }
                if (!roomId1.getGroupList().isEmpty()) roomsId1.add(roomId1);
                if (!roomId1.getGroupList().isEmpty()) roomsId2.add(roomId2);
            }
        }
        List<RoomDto> roomDtosList1 = roomsId1.stream().map(mapper::toDto).toList();
        List<RoomDto> roomDtosList2 = roomsId2.stream().map(mapper::toDto).toList();

        List<WeekTableDto> weekTableDtoList = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            WeekTableDto weekTableDto = new WeekTableDto();
            if (i % 2 != 0) {
                weekTableDto = new WeekTableDto(generateIdByWeek(i), roomDtosList1);
            } else
                weekTableDto = new WeekTableDto(generateIdByWeek(i), roomDtosList2);
            weekTableDtoList.add(weekTableDto);
        }
        return ResponseDto.<List<WeekTableDto>>builder()
                .code(0)
                .message("OK")
                .success(true)
                .data(weekTableDtoList)
                .build();
    }


    public ResponseDto<WeekTableDto> getByIdWeeks(Integer id) { //method darslarni xafta kunlarini boyicha qaytaradi
        Integer weekId = 2;
        WeekTableDto weekTableDto = new WeekTableDto();
        weekTableDto.setWeek_names(generateIdByWeek(id));
        if (id % 2 != 0) {
            weekId = 1;
        }
        List<RoomDto> roomDtoList = repository.findAll().stream().map(mapper::toDto).
                filter(iteam -> iteam.getGroupList().isEmpty()).toList();
        for (int i = 0; i < roomDtoList.size(); i++) {
            Integer finalWeekId = weekId;
            roomDtoList.get(i).setGroupList(roomDtoList.get(i).getGroupList().stream()
                    .filter(iteam -> iteam.getWeekId().equals(finalWeekId))
                    .collect(Collectors.toList()));
        }
        weekTableDto.setRoomDtos(roomDtoList);
        return ResponseDto.<WeekTableDto>builder()
                .code(0)
                .message("OK")
                .success(true)
                .data(weekTableDto)
                .build();
    }

    public String generateIdByWeek(Integer id) { //method darslarni xafta kunlari bo'yciah qaytarish uchun .
        switch (id) {
            case 1:
                return "Monday";
            case 2:
                return "Thuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
        }
        return null;
    }

    //bunda biz gr ni larni xammasini xaftalik qilib chiqarish uchun , room ni detail larini qo'limizda kiritib chiqdik .
    public Room map(Room room) {
        Room export = new Room();
        export.setId(room.getId());
        export.setName(room.getName());
        export.setBooked(room.getBooked());
        export.setGroupList(new ArrayList<>());
        return export;
    }
}
