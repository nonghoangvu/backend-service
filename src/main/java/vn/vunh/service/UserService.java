/*
 * Author: Nong Hoang Vu || JavaTech
 * Facebook:https://facebook.com/NongHoangVu04
 * Github: https://github.com/JavaTech04
 * Youtube: https://www.youtube.com/@javatech04/?sub_confirmation=1
 */
package vn.vunh.service;

import vn.vunh.controller.request.UserCreationRequest;
import vn.vunh.controller.request.UserPasswordRequest;
import vn.vunh.controller.request.UserUpdateRequest;
import vn.vunh.controller.response.UserPageResponse;
import vn.vunh.controller.response.UserResponse;
import vn.vunh.model.elasticsearch.UserEntityDocument;

import java.util.List;

public interface UserService {

    UserPageResponse findAll(String keyword, String sort, int page, int size);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    long save(UserCreationRequest req);

    void update(UserUpdateRequest req);

    void changePassword(UserPasswordRequest req);

    void delete(Long id);

    List<UserEntityDocument> findAll();
}
