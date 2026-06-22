package com.example.ms_users.service;

import com.example.ms_users.dto.request.UserRequestDTO;
import com.example.ms_users.dto.request.UserUpdateDTO;
import com.example.ms_users.dto.response.UserResponseDTO;
import com.example.ms_users.exception.custom.EmailAlreadyExistsException;
import com.example.ms_users.exception.custom.InvalidRoleException;
import com.example.ms_users.exception.custom.UserNotFoundException;
import com.example.ms_users.exception.custom.UsernameAlreadyExistsException;
import com.example.ms_users.model.Role;
import com.example.ms_users.model.User;
import com.example.ms_users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== VALIDACIONES DE EXISTENCIA ==========
    public boolean existsById(Long id) {
        log.debug("Verificando existencia de usuario por ID: {}", id);
        return repository.existsById(id);
    }

    public boolean existsByUsername(String username) {
        log.debug("Verificando existencia de username: {}", username);
        return repository.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        log.debug("Verificando existencia de email: {}", email);
        return repository.findByEmail(email).isPresent();
    }

    // ========== REGISTRO ==========

    public User register(String username, String password, Role role, String email, String phone, String address) {
        log.info("Registrando nuevo usuario: {}", username);

        // Verificaciones adicionales
        if (existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("El username '" + username + "' ya está en uso");
        }
        if (existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya está registrado");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role != null ? role : Role.CLIENT)
                .phone(phone)
                .address(address)
                .build();
        User saved = repository.save(user);
        log.info("Usuario registrado con éxito: {} (id: {})", username, saved.getUserId());
        return saved;
    }

    // ========== BÚSQUEDAS ==========

    public List<User> findAll() {
        log.debug("Obteniendo todos los usuarios");
        List<User> users = repository.findAll();
        log.debug("Total de usuarios encontrados: {}", users.size());
        return users;
    }

    public Optional<User> findById(Long id) {
        log.debug("Buscando usuario por id: {}", id);
        return repository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return repository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        return repository.findByUsername(username);
    }

    public User findByUsernameOrThrow(String username) {
        log.debug("Buscando usuario por username (con excepción): {}", username);
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));
    }

    // ========== ACTUALIZACIONES ==========

    public User save(User user) {
        log.info("Guardando usuario: {}", user.getUsername());
        User saved = repository.save(user);
        log.debug("Usuario guardado con id: {}", saved.getUserId());
        return saved;
    }

    public User update(Long id, UserRequestDTO dto) {
        log.info("Actualizando usuario con id: {}", id);
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            log.debug("Contraseña actualizada para usuario: {}", user.getUsername());
        }
        user.setPhone(dto.getPhone());
        User updated = repository.save(user);
        log.info("Usuario actualizado: {}", updated.getUsername());
        return updated;
    }

    @Transactional
    public User updateUserProfile(String username, UserUpdateDTO updateDTO) {
        log.info("Actualizando perfil de usuario: {}", username);
        User user = findByUsernameOrThrow(username);

        if (updateDTO.getEmail() != null && !updateDTO.getEmail().isBlank()) {
            // Verificar que el email no esté en uso por otro usuario
            Optional<User> existingUser = repository.findByEmail(updateDTO.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getUsername().equals(username)) {
                throw new EmailAlreadyExistsException("El email '" + updateDTO.getEmail() + "' ya está en uso por otro usuario");
            }
            user.setEmail(updateDTO.getEmail());
            log.debug("Email actualizado para {}: {}", username, updateDTO.getEmail());
        }

        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
            log.debug("Teléfono actualizado para {}", username);
        }

        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }

        User saved = repository.save(user);
        log.info("Perfil actualizado para usuario: {}", username);
        return saved;
    }

    // ========== ELIMINACIÓN ==========

    public void deleteById(Long id) {
        log.info("Eliminando usuario por id: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            log.info("Usuario con id {} eliminado", id);
        } else {
            log.warn("Intento de eliminar usuario inexistente con id: {}", id);
            throw new UserNotFoundException("Usuario no encontrado con id: " + id);
        }
    }

    @Transactional
    public void deleteByUsername(String username) {
        log.warn("Eliminando usuario por username: {}", username);
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));
        repository.deleteByUsername(username);
        log.info("Usuario eliminado: {}", username);
    }

    // ========== NUEVOS MÉTODOS PARA ADMIN CONTROLLER ==========

    public List<UserResponseDTO> getAllUsers() {
        log.debug("Obteniendo todos los usuarios para ADMIN");
        return findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserProfile(String username) {
        log.debug("Obteniendo perfil de usuario: {}", username);
        User user = findByUsernameOrThrow(username);
        return mapToResponseDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        User user = findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
        return mapToResponseDTO(user);
    }

    public List<UserResponseDTO> getUsersByRole(String role) {
        log.debug("Buscando usuarios por rol: {}", role);
        Role validRole;
        try {
            validRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT");
        }

        return findAll().stream()
                .filter(user -> user.getRole() == validRole)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(String username, UserUpdateDTO updateDTO) {
        log.info("Actualizando usuario (admin): {}", username);
        User updatedUser = updateUserProfile(username, updateDTO);
        return mapToResponseDTO(updatedUser);
    }

    public UserResponseDTO changeRole(String username, String newRole) {
        log.info("Cambiando rol de usuario: {} a {}", username, newRole);
        User user = findByUsernameOrThrow(username);

        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            user.setRole(role);
            User saved = repository.save(user);
            log.info("Rol cambiado exitosamente para: {} -> {}", username, newRole);
            return mapToResponseDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Rol inválido. Use: ADMIN, EMPLOYEE o CLIENT");
        }
    }

    public void deleteOwnAccount(String username) {
        log.warn("Eliminando cuenta propia de usuario: {}", username);
        deleteByUsername(username);
    }

    // ========== MeTODO UTILITARIO PRIVADO ==========

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}