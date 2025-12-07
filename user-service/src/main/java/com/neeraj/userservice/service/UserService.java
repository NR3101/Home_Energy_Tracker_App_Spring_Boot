package com.neeraj.userservice.service;

import com.neeraj.userservice.dto.UserDTO;
import com.neeraj.userservice.entity.User;
import com.neeraj.userservice.exception.EmailAlreadyExistsException;
import com.neeraj.userservice.exception.UserNotFoundException;
import com.neeraj.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("User with email " + userDTO.getEmail() + " already exists");
        }

        final User createdUser = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .address(userDTO.getAddress())
                .alertEnabled(userDTO.getAlertEnabled())
                .energyAlertThreshold(userDTO.getEnergyAlertThreshold())
                .build();

        final User savedUser = userRepository.save(createdUser);
        return toUserDTO(savedUser);
    }

    private UserDTO toUserDTO(User savedUser) {
        return UserDTO.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .address(savedUser.getAddress())
                .alertEnabled(savedUser.getAlertEnabled())
                .energyAlertThreshold(savedUser.getEnergyAlertThreshold())
                .build();
    }

    public UserDTO getUser(Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return toUserDTO(user);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setAlertEnabled(userDTO.getAlertEnabled());
        user.setEnergyAlertThreshold(userDTO.getEnergyAlertThreshold());

        final User updatedUser = userRepository.save(user);
        return toUserDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }
}
