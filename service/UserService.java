package com.library.service;

import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User register(User user);
    User findById(Long id);
    User findByUsername(String username);
    User update(User user);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void toggleEnabled(Long id);
    void deleteUser(Long id);
    Page<User> findAll(Pageable pageable);
    Page<User> search(String q, Pageable pageable);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
