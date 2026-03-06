package com.malgn.service;

import com.malgn.dto.UserRequest;
import com.malgn.dto.UserResponse;
import com.malgn.entity.Role;
import com.malgn.entity.User;
import com.malgn.exception.NotAuthorizedException;
import com.malgn.exception.UserNotFoundException;
import com.malgn.repository.UserRepository;
import com.malgn.security.CmsUserDetails;
import com.malgn.util.EntityDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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

        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        if (request.username().equals("admin")) {
            roles.add(Role.ROLE_ADMIN);
        }

        User savedUser = userRepository.save(
                User.builder()
                        .username(request.username())
                        .password(request.password())
                        .roles(roles)
                        .build()
        );

        return EntityDtoMapper.toDto(savedUser);
    }

    public UserResponse updateUser(CmsUserDetails userDetails, Long userId, UserRequest request) {
        if (userDetails.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()))
        && !userDetails.getId().equals(userId)) {
            throw new NotAuthorizedException("권한이 없습니다.");
        }

        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)
                ));

        user.setUsername(request.username());
        user.setPassword(request.password());

        return EntityDtoMapper.toDto(user);
    }

    public void deleteUser(Long userId) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)
                ));

        userRepository.deleteById(user.getId());
    }

    public UserResponse getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(EntityDtoMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 이름(%s)을 가진 사용자를 찾을 수 없습니다.", username)
                ));
    }
}
