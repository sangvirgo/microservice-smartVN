package com.smartvn.admin_service.dto.user;

import lombok.Data;

@Data
public class UserStatsDTO {
    private long totalUsers;
    private long totalCustomers;
    private long totalStaff;
    private long totalAdmins;
    private long bannedUsers;
    private long inactiveUsers;
    // Thêm các thống kê khác nếu cần (ví dụ: new users today/week/month)
}