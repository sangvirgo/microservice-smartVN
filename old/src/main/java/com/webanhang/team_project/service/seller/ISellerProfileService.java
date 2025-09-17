package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.seller.SellerProfileDTO;
import com.webanhang.team_project.dto.seller.UpdateSellerProfileRequest;

import java.util.Map;

public interface ISellerProfileService {
    SellerProfileDTO getSellerProfile(Long sellerId);
    SellerProfileDTO updateSellerProfile(Long sellerId, UpdateSellerProfileRequest request);
    SellerProfileDTO.ShopInfoDTO getShopInfo(Long sellerId);
    SellerProfileDTO.ShopInfoDTO updateShopInfo(Long sellerId, UpdateSellerProfileRequest.ShopInfo shopInfo);
    Map<String, Object> getVerificationStatus(Long sellerId);
}
