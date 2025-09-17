package com.webanhang.team_project.service.user;


import com.webanhang.team_project.dto.address.AddAddressRequest;
import com.webanhang.team_project.dto.address.AddressDTO;
import com.webanhang.team_project.dto.user.BasicUserDTO;
import com.webanhang.team_project.dto.user.UserDTO;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.dto.user.CreateUserRequest;
import com.webanhang.team_project.dto.auth.OtpVerificationRequest;
import com.webanhang.team_project.dto.auth.RegisterRequest;
import com.webanhang.team_project.dto.user.UpdateUserRequest;

public interface IUserService {
    User createUser(CreateUserRequest request);
    UserDTO updateUser(UpdateUserRequest request, Long userId);
    User getUserById(Long userId);
    void deleteUser(Long userId);

    UserDTO convertUserToDto(User user);

    void registerUser(RegisterRequest request);
    boolean verifyOtp(OtpVerificationRequest request);

    User findUserByJwt(String jwt);
    UserDTO findUserProfileByJwt(String jwt);

    AddressDTO addUserAddress(User user, AddAddressRequest request);
    BasicUserDTO changeUserRole(Long userId, String roleName);
}
