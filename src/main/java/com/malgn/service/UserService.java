package com.malgn.service;

import com.malgn.dto.UserRequest;
import com.malgn.dto.UserResponse;
import com.malgn.entity.User;
import com.malgn.exception.UserNotFoundException;
import com.malgn.repository.UserRepository;
import com.malgn.util.EntityDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(EntityDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(EntityDtoMapper::toDto);
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
        }

        User savedUser = userRepository.save(
                User.builder()
                        .username(request.username())
                        .password(request.password())
                        .build()
        );

        return EntityDtoMapper.toDto(savedUser);
    }

    public UserResponse updateUser(Long userId, UserRequest request) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)
                ));

        user.setUsername(request.username());
        user.setPassword(request.password());

        User updatedUser = userRepository.save(user);
        return EntityDtoMapper.toDto(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)
                ));

        userRepository.deleteById(user.getId());
    }
}
