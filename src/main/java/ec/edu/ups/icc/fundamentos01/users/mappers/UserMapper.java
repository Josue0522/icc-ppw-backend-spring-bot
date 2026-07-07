package ec.edu.ups.icc.fundamentos01.users.mappers;

import ec.edu.ups.icc.fundamentos01.users.dto.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;

public class UserMapper {

    public static UserEntity toEntity(CreateUserDto dto) {
        UserEntity entity = new UserEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(dto.getPassword());
        return entity;
    }

    public static void updateEntity(UserEntity entity, UpdateUserDto dto) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        //entity.setPasswordHash("HASH_"+dto.getPassword());
    }

    public static void partialUpdateEntity(UserEntity entity, PartialUpdateUserDto dto) {
    if (dto.getName() != null) {
        entity.setName(dto.getName());
    }

    if (dto.getEmail() != null) {
        entity.setEmail(dto.getEmail());
    }

    if (dto.getPassword() != null) {
        entity.setPasswordHash("Hash - "+dto.getPassword());
    }
}

    public static UserResponseDto toResponseDto(UserEntity entity) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}