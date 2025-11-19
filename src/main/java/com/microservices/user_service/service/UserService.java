package com.microservices.user_service.service;

import com.microservices.user_service.dto.UserDTO;
import com.microservices.user_service.dto.UserDTOWithCards;
import com.microservices.user_service.exception.DuplicateResourceException;
import com.microservices.user_service.exception.NotFoundException;
import com.microservices.user_service.mapper.UserMapper;
import com.microservices.user_service.model.User;
import com.microservices.user_service.repository.UserRepository;
import com.microservices.user_service.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        if (!securityUtils.hasAccessToUser(id)) {
            throw new AccessDeniedException("Access denied");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return userMapper.toDTO(user);
    }

    @Cacheable(value = "usersWithCards", key = "#id")
    @Transactional(readOnly = true)
    public UserDTOWithCards getUserWithCardsById(Long id) {
        User user = userRepository.findByIdWithCards(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return userMapper.toDTOWithCards(user);
    }

    @Cacheable(value = "users", key = "#email")
    @Transactional(readOnly = true)
    public UserDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    @CacheEvict(value = {"users", "usersWithCards"}, allEntries = true)
    @Transactional
    public UserDTO create(UserDTO userDTO) {
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", userDTO.getEmail());
        }
        User user = userMapper.toEntity(userDTO);
        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @CacheEvict(value = {"users", "usersWithCards"}, key = "#id")
    @Transactional
    public UserDTO update(Long id, UserDTO userDTO) {
        if (!securityUtils.hasAccessToUser(id)) {
            throw new AccessDeniedException("You can only update your own profile");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        if (userDTO.getEmail() != null &&
                !user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.findByEmail(userDTO.getEmail()).isPresent()) {

            throw new DuplicateResourceException("User", "email", userDTO.getEmail());
        }
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setBirthDate(userDTO.getBirthDate());
        user.setEmail(userDTO.getEmail());

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @CacheEvict(value = {"users", "usersWithCards"}, key = "#id")
    @Transactional
    public void deleteById(Long id) {
        if (!securityUtils.hasAccessToUser(id)) {
            throw new AccessDeniedException("You can only delete your own profile");
        }
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    @Cacheable(value = "users", key = "'page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only admins can view all users");
        }
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toDTO);
    }
}
