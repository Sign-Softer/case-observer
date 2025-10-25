package ro.signsofter.caseobserver.controller.mapper;

import ro.signsofter.caseobserver.controller.dto.UserDto;
import ro.signsofter.caseobserver.entity.User;

public class UserMapper {

    public static UserDto toDto(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole().name());
        dto.setIsActive(u.getIsActive());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setUpdatedAt(u.getUpdatedAt());
        return dto;
    }

    public static User fromDto(UserDto dto) {
        User u = new User();
        u.setId(dto.getId());
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        if (dto.getRole() != null) {
            u.setRole(User.Role.valueOf(dto.getRole()));
        }
        u.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        return u;
    }
}




