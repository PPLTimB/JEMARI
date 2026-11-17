package com.jemari.user;

import org.springframework.stereotype.Service;

import com.jemari.common.exception.ResourceNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }

    public void deactivate(Long userId) {
        User user = getById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activate(Long userId) {
        User user = getById(userId);
        user.setActive(true);
        userRepository.save(user);
    }
}