package com.smartvn.user_service.service.user;

import com.smartvn.user_service.dto.address.AddAddressRequest;
import com.smartvn.user_service.dto.address.AddressDTO;
import com.smartvn.user_service.dto.auth.OtpVerificationRequest;
import com.smartvn.user_service.dto.auth.RegisterRequest;
import com.smartvn.user_service.dto.internal.UserInfoDTO;
import com.smartvn.user_service.dto.response.ApiResponse;
import com.smartvn.user_service.dto.user.UpdateUserRequest;
import com.smartvn.user_service.dto.user.UserDTO;
import com.smartvn.user_service.dto.user.UserStatsDTO;
import com.smartvn.user_service.enums.UserRole;
import com.smartvn.user_service.exceptions.AppException;
import com.smartvn.user_service.model.Address;
import com.smartvn.user_service.model.Role;
import com.smartvn.user_service.model.User;
import com.smartvn.user_service.repository.AddressRepository;
import com.smartvn.user_service.repository.RoleRepository;
import com.smartvn.user_service.repository.UserRepository;
import com.smartvn.user_service.security.jwt.JwtUtils;
import com.smartvn.user_service.service.otp.OtpService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AddressRepository addressRepository;
    private final JwtUtils jwtUtils;
//    @Autowired
//    private EntityManager entityManager; // ✅ Thêm

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

    public boolean verifyOtp(OtpVerificationRequest request) {
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (isValid) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("User not found for OTP verification"));
            user.setActive(true);
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        return isValid;
    }

    public void forgotPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findUserByJwt(String jwt) {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        String email = jwtUtils.getEmailFromToken(jwt);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email from JWT: " + email));
    }

    public UserDTO findUserProfileByJwt(String jwt) {
        User user = findUserByJwt(jwt);
        return convertUserToDto(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UpdateUserRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));

        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setPhone(request.getPhoneNumber());
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        return convertUserToDto(updatedUser);
    }

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

    public Page<User> searchUsers(String search, String role,
                                  Boolean isBanned, Pageable pageable) {

        Specification<User> spec = Specification.where(null);

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("email")), searchPattern),
                            cb.like(cb.lower(root.get("firstName")), searchPattern),
                            cb.like(cb.lower(root.get("lastName")), searchPattern)
                    )
            );
        }

        if (role != null) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("role").get("name"), userRole)
                );
            } catch (IllegalArgumentException e) {
                throw new AppException("Invalid role: " + role,
                        HttpStatus.BAD_REQUEST);
            }
        }

        if (isBanned != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isBanned"), isBanned)
            );
        }

        return userRepository.findAll(spec, pageable);
    }

    public void banUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
        user.setBanned(true);
        userRepository.save(user);
    }

    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
        user.setBanned(false);
        userRepository.save(user);
    }

    public void changeRole(Long userId, UserRole userRole) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
        Role targetRole = roleRepository.findByName(userRole)
                .orElseThrow(() -> new EntityNotFoundException("Role not found in database: " + userRole.name()));
        user.setRole(targetRole);
        userRepository.save(user);
    }

    @Transactional
    public void incrementWarningCount(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));

        user.setWarningCount(user.getWarningCount() + 1);
        if (user.getWarningCount() >= 3 && !user.isBanned()) {
            user.setBanned(true);
        }

        userRepository.save(user);
//        userRepository.flush(); // ✅ Force flush
//        entityManager.clear(); // ✅ Clear cache
    }

    public UserStatsDTO calculateUserStats() {
        UserStatsDTO stats = new UserStatsDTO();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCustomers(userRepository.countByRole_Name(UserRole.CUSTOMER));
        stats.setTotalStaff(userRepository.countByRole_Name(UserRole.STAFF));
        stats.setTotalAdmins(userRepository.countByRole_Name(UserRole.ADMIN));
        stats.setBannedUsers(userRepository.countByIsBanned(true));
        stats.setInactiveUsers(userRepository.countByActive(false));
        return stats;
    }

    // Hàm helper private, không cần nằm trong interface
    public UserDTO convertUserToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole() != null ? user.getRole().getName().name() : null);
        userDTO.setMobile(user.getPhone());
        userDTO.setActive(user.isActive());
        userDTO.setWarningCount(user.getWarningCount());
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
                user.getImageUrl(),
                user.isActive(),
                user.isBanned()
        );
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
        return convertUserToDto(user);
    }

    @Transactional
    public void softDeleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        // Kiểm tra quyền sở hữu
        if (!address.getUser().getId().equals(userId)) {
            throw new AppException("Unauthorized", HttpStatus.FORBIDDEN);
        }

        // Soft delete
        address.setIsActive(false);
        addressRepository.save(address);
    }
}