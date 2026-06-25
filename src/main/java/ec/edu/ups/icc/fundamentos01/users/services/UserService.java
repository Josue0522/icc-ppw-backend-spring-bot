package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.users.dto.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UserResponseDto;

public interface UserService {

    List<UserResponseDto> findAll();

    UserResponseDto findOne(Long id);

    UserResponseDto create(CreateUserDto dto);

    UserResponseDto update(Long id, UpdateUserDto dto);

    UserResponseDto partialUpdate(Long id, PartialUpdateUserDto dto);

    void delete(Long id);
}