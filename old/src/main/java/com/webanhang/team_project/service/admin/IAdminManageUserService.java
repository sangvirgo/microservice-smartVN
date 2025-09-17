package com.webanhang.team_project.service.admin;

import com.webanhang.team_project.dto.user.UpdateUserInfoRequest;
import com.webanhang.team_project.dto.user.UserDTO;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface IAdminManageUserService {
    Page<UserDTO> getAllUsers(int page, int size, String search, String role);

    UserDTO getUserDetails(Long userId);

    UserDTO updateUserInfo(Long userId, UpdateUserInfoRequest request);

    UserDTO changeUserRole(Long userId, String roleName);

    UserDTO updateUserStatus(Long userId, boolean active);

    void deleteUser(Long userId);

    Map<String, Object> getCustomerStatistics();

    UserDTO banUser(Long userId, boolean banned);
}
