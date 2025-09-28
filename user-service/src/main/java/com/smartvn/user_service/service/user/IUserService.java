package com.smartvn.user_service.service.user;

import com.smartvn.user_service.dto.address.AddAddressRequest;
import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.auth.OtpVerificationRequest;
import com.smartvn.user_service.dto.auth.RegisterRequest;
import com.smartvn.user_service.dto.user.UpdateUserRequest;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.model.User;

public interface IUserService {

    void registerUser(RegisterRequest request);

    boolean verifyOtp(OtpVerificationRequest request);

    void forgotPassword(String email, String newPassword);

    User findUserByJwt(String jwt);

    UserDTO findUserProfileByJwt(String jwt);

    UserDTO updateUser(Long userId, UpdateUserRequest request);

    AddressDTO addUserAddress(Long userId, AddAddressRequest request);
}