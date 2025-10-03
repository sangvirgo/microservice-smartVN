package com.smartvn.user_service.service.user;

import com.smartvn.user_service.dto.address.AddAddressRequest;
import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.auth.OtpVerificationRequest;
import com.smartvn.user_service.dto.auth.RegisterRequest;
import com.smartvn.user_service.dto.internal.UserInfoDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UpdateUserRequest;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.model.Role;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.AddressRepository;
import com.smartvn.user_service.repository.RoleRepository;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.security.jwt.JwtUtils;
import com.smartvn.user_service.service.otp.OtpService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AddressRepository addressRepository;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("Email '" + request.getEmail() + "' đã được sử dụng.");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);

        Role role = roleRepository.findByName(UserRole.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.CUSTOMER)));
        user.setRole(role);
        userRepository.save(user);

        String otp = otpService.generateOtp(request.getEmail());
        otpService.sendOtpEmail(request.getEmail(), otp);
    }

    @Override
    public boolean verifyOtp(OtpVerificationRequest request) {
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (isValid) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found for OTP verification"));
            user.setActive(true);
            userRepository.save(user);
        }
        return isValid;
    }

    @Override
    public void forgotPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public User findUserByJwt(String jwt) {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        String email = jwtUtils.getEmailFromToken(jwt);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email from JWT: " + email));
    }

    @Override
    public UserDTO findUserProfileByJwt(String jwt) {
        User user = findUserByJwt(jwt);
        return convertUserToDto(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhone(request.getPhoneNumber());

        User updatedUser = userRepository.save(existingUser);
        return convertUserToDto(updatedUser);
    }

    @Override
    @Transactional
    public AddressDTO addUserAddress(Long userId, AddAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Address newAddress = new Address();
        newAddress.setFullName(request.getFullName());
        newAddress.setProvince(request.getProvince());
        newAddress.setWard(request.getWard());
        newAddress.setStreet(request.getStreet());
        newAddress.setNote(request.getNote());
        newAddress.setPhoneNumber(request.getPhoneNumber());
        newAddress.setUser(user);
        
        Address savedAddress = addressRepository.save(newAddress);
        return new AddressDTO(savedAddress);
    }

    // Hàm helper private, không cần nằm trong interface
    private UserDTO convertUserToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole() != null ? user.getRole().getName().name() : null);
        userDTO.setMobile(user.getPhone());
        userDTO.setActive(user.isActive());
        userDTO.setBanned(user.isBanned());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setImageUrl(user.getImageUrl());
        userDTO.setOauthProvider(user.getOauthProvider());

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(AddressDTO::new)
                    .collect(Collectors.toList()));
        }

        return userDTO;
    }

    public UserInfoDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));

        return new UserInfoDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getImageUrl()
        );
    }
}