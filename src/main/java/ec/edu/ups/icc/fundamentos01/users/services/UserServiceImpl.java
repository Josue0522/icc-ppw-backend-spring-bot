package ec.edu.ups.icc.fundamentos01.users.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.users.dto.CreateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.PartialUpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UpdateUserDto;
import ec.edu.ups.icc.fundamentos01.users.dto.UserResponseDto;
import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.mappers.UserMapper;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;
import ec.edu.ups.icc.fundamentos01.users.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.isDeleted())
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto findOne(Long id) {
        UserEntity user = findActiveUserById(id);
        return UserMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto create(CreateUserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        UserEntity user = UserMapper.toEntity(dto);
        UserEntity savedUser = userRepository.save(user);

        return UserMapper.toResponseDto(savedUser);
    }

    @Override
    public UserResponseDto update(Long id, UpdateUserDto dto) {
        UserEntity user = findActiveUserById(id);

        userRepository.findByEmail(dto.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .ifPresent(existingUser -> {
                    throw new IllegalStateException("Email already registered");
                });

        UserMapper.updateEntity(user, dto);

        UserEntity updatedUser = userRepository.save(user);
        return UserMapper.toResponseDto(updatedUser);
    }

    @Override
    public UserResponseDto partialUpdate(Long id, PartialUpdateUserDto dto) {
        UserEntity user = findActiveUserById(id);

        if (dto.getEmail() != null) {
            userRepository.findByEmail(dto.getEmail())
                    .filter(existingUser -> !existingUser.getId().equals(id))
                    .ifPresent(existingUser -> {
                        throw new IllegalStateException("Email already registered");
                    });
        }

        UserMapper.partialUpdateEntity(user, dto);

        UserEntity updatedUser = userRepository.save(user);
        return UserMapper.toResponseDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (user.isDeleted()) {
            throw new IllegalStateException("El usuario ya fue eliminado");
        }

        user.setDeleted(true);
        userRepository.save(user);
    }

    private UserEntity findActiveUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (user.isDeleted()) {
            throw new IllegalStateException("El usuario está eliminado");
        }

        return user;
    }
}