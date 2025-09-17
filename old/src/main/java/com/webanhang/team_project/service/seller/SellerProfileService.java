package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.seller.SellerProfileDTO;
import com.webanhang.team_project.dto.seller.UpdateSellerProfileRequest;
import com.webanhang.team_project.model.Address;
import com.webanhang.team_project.model.User;
import com.webanhang.team_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerProfileService implements ISellerProfileService {

    private final UserRepository userRepository;

    @Override
    public SellerProfileDTO getSellerProfile(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        return convertToProfileDTO(seller);
    }

    @Override
    @Transactional
    public SellerProfileDTO updateSellerProfile(Long sellerId, UpdateSellerProfileRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        // Update personal information
        if (request.getFirstName() != null) {
            seller.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            seller.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            seller.setPhone(request.getPhone());
        }

        // Update shop information if provided in the request
        if (request.getShopInfo() != null) {
            UpdateSellerProfileRequest.ShopInfo shopInfo = request.getShopInfo();

            if (shopInfo.getDescription() != null) {
                seller.setShopDescription(shopInfo.getDescription());
            }
            if (shopInfo.getWebsite() != null) {
                seller.setWebsite(shopInfo.getWebsite());
            }
            if (shopInfo.getBusinessType() != null) {
                seller.setBusinessType(shopInfo.getBusinessType());
            }
            if (shopInfo.getPhone() != null) {
                seller.setPhone(shopInfo.getPhone());
            }
        }

        User updatedSeller = userRepository.save(seller);
        return convertToProfileDTO(updatedSeller);
    }

    @Override
    public SellerProfileDTO.ShopInfoDTO getShopInfo(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        return buildShopInfoDTO(seller);
    }

    @Override
    @Transactional
    public SellerProfileDTO.ShopInfoDTO updateShopInfo(Long sellerId, UpdateSellerProfileRequest.ShopInfo shopInfo) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        // Update shop-specific fields
        if (shopInfo.getShopName() != null && !shopInfo.getShopName().trim().isEmpty()) {
            seller.setShopName(shopInfo.getShopName());
        }
        if (shopInfo.getDescription() != null && !shopInfo.getDescription().trim().isEmpty()) {
            seller.setShopDescription(shopInfo.getDescription());
        }
        if (shopInfo.getWebsite() != null && !shopInfo.getWebsite().trim().isEmpty()) {
            seller.setWebsite(shopInfo.getWebsite());
        }
        if (shopInfo.getBusinessType() != null && !shopInfo.getBusinessType().trim().isEmpty()) {
            seller.setBusinessType(shopInfo.getBusinessType());
        }
        if (shopInfo.getPhone() != null && !shopInfo.getPhone().trim().isEmpty()) {
            seller.setPhone(shopInfo.getPhone());
        }

        User updatedSeller = userRepository.save(seller);
        return buildShopInfoDTO(updatedSeller);
    }

    @Override
    public Map<String, Object> getVerificationStatus(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        Map<String, Object> status = new HashMap<>();
        status.put("verified", seller.isActive());
        status.put("verificationDate", seller.getCreatedAt());
        status.put("verificationStatus", seller.isActive() ? "Đã xác minh" : "Chưa xác minh");

        return status;
    }

    // Helper method to build ShopInfoDTO from User entity
    private SellerProfileDTO.ShopInfoDTO buildShopInfoDTO(User seller) {
        // Generate shop name from user's name
        String shopName = generateShopName(seller);

        // Use imageUrl as logo
        String logo = seller.getImageUrl() != null ? seller.getImageUrl() : "";

        // Use shop description or generate default
        String description = seller.getShopDescription() != null ?
                seller.getShopDescription() :
                generateDefaultShopDescription(seller);

        // Get address components from user's first address
        Address primaryAddress = getPrimaryAddress(seller);

        return new SellerProfileDTO.ShopInfoDTO(
                shopName,
                logo,
                description,
                seller.getWebsite() != null ? seller.getWebsite() : "",
                primaryAddress != null ? primaryAddress.getStreet() : "",
                primaryAddress != null ? primaryAddress.getDistrict() : "",
                primaryAddress != null ? primaryAddress.getProvince() : "",
                primaryAddress != null ? primaryAddress.getWard() : "",
                seller.getPhone() != null ? seller.getPhone() : "",
                seller.getBusinessType() != null ? seller.getBusinessType() : "Cá nhân"
        );
    }

    // Helper method to convert User to SellerProfileDTO
    private SellerProfileDTO convertToProfileDTO(User seller) {
        SellerProfileDTO profileDTO = new SellerProfileDTO();
        profileDTO.setId(seller.getId());
        profileDTO.setFirstName(seller.getFirstName());
        profileDTO.setLastName(seller.getLastName());
        profileDTO.setEmail(seller.getEmail());
        profileDTO.setPhone(seller.getPhone());
        profileDTO.setVerified(seller.isActive());
        profileDTO.setCreatedAt(seller.getCreatedAt() != null ?
                seller.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");

        // Include shop information
        profileDTO.setShopInfo(buildShopInfoDTO(seller));

        return profileDTO;
    }

    // Helper method to generate shop name
    private String generateShopName(User seller) {
        // Use custom shop name if available, otherwise generate default
        if (seller.getShopName() != null && !seller.getShopName().trim().isEmpty()) {
            return seller.getShopName();
        } else if (seller.getFirstName() != null && seller.getLastName() != null) {
            return seller.getFirstName() + " " + seller.getLastName() + " Shop";
        } else if (seller.getFirstName() != null) {
            return seller.getFirstName() + " Shop";
        } else {
            return "My Shop";
        }
    }

    // Helper method to generate default shop description
    private String generateDefaultShopDescription(User seller) {
        if (seller.getFirstName() != null) {
            return "Cửa hàng của " + seller.getFirstName() +
                    (seller.getLastName() != null ? " " + seller.getLastName() : "") +
                    " - Chuyên cung cấp các sản phẩm chất lượng cao";
        }
        return "Cửa hàng chuyên cung cấp các sản phẩm chất lượng cao";
    }

    // Helper method to get primary address
    private Address getPrimaryAddress(User seller) {
        if (seller.getAddress() != null && !seller.getAddress().isEmpty()) {
            return seller.getAddress().get(0); // Use first address as primary
        }
        return null;
    }
}