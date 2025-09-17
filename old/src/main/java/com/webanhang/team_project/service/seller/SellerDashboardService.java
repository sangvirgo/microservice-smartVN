package com.webanhang.team_project.service.seller;

import com.webanhang.team_project.dto.seller.OrderStatsDTO;
import com.webanhang.team_project.dto.seller.SellerDashboardDTO;
import com.webanhang.team_project.enums.OrderStatus;
import com.webanhang.team_project.model.*;
import com.webanhang.team_project.repository.OrderRepository;
import com.webanhang.team_project.repository.ProductRepository;
import com.webanhang.team_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerDashboardService implements ISellerDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardDTO getDashboardData(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        BigDecimal totalRevenue = calculateTotalRevenue(sellerId);
        Integer totalOrders = countTotalOrders(sellerId);
        Integer totalProducts = countTotalProducts(sellerId);
        Integer totalCustomers = countTotalCustomers(sellerId);

        List<OrderStatsDTO> recentOrders = getRecentOrders(sellerId);

        Map<String, BigDecimal> revenueByWeek = getRevenueByWeek(sellerId);

        Map<String, BigDecimal> revenueByMonth = getRevenueByMonth(sellerId);

        return new SellerDashboardDTO(
                totalRevenue,
                totalOrders,
                totalProducts,
                totalCustomers,
                recentOrders,
                revenueByWeek,
                revenueByMonth
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyRevenue(Long sellerId) {
        Map<String, BigDecimal> revenueData = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        // Initialize map with zero values
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthLabel = month.format(formatter);
            revenueData.put(monthLabel, BigDecimal.ZERO);
        }

        // Get orders for THIS SELLER ONLY with date range
        LocalDateTime startDate = now.minusMonths(11).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = now.atTime(23, 59, 59);

        List<Order> sellerOrders = orderRepository.findBySellerId(sellerId);
        List<Order> filteredOrders = sellerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .toList();

        // Calculate revenue using same logic as total revenue
        for (Order order : filteredOrders) {
            String monthKey = order.getOrderDate().format(formatter);
            if (revenueData.containsKey(monthKey)) {
                BigDecimal orderRevenue = BigDecimal.valueOf(order.getTotalDiscountedPrice());
                revenueData.put(monthKey, revenueData.get(monthKey).add(orderRevenue));
            }
        }

        return revenueData;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getRevenueByWeek(Long sellerId) {
        Map<String, BigDecimal> revenueByWeek = new LinkedHashMap<>();

        // Lấy dữ liệu 6 tuần gần nhất
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Khởi tạo map với các tuần và giá trị 0
        for (int i = 5; i >= 0; i--) {
            LocalDate weekStart = now.minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            String weekLabel = weekStart.format(formatter) + " - " + weekEnd.format(formatter);
            revenueByWeek.put(weekLabel, BigDecimal.ZERO);
        }

        // Lấy đơn hàng từ 6 tuần trước
        LocalDateTime startDate = now.minusWeeks(5).atStartOfDay();
        LocalDateTime endDate = now.atTime(23, 59, 59);
        List<Order> allOrders = orderRepository.findByOrderDateBetweenAndOrderStatus(
                startDate, endDate, OrderStatus.DELIVERED);

        // Lọc đơn hàng có sản phẩm của seller
        for (Order order : allOrders) {
            // Kiểm tra xem đơn hàng có chứa sản phẩm của seller hay không
            boolean hasSellersProduct = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getSellerId().equals(sellerId));

            if (hasSellersProduct) {
                // Tính doanh thu chỉ từ sản phẩm của seller trong đơn hàng này
                BigDecimal orderRevenue = order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getSellerId().equals(sellerId))
                        .map(item -> BigDecimal.valueOf(item.getPrice() * item.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Xác định tuần của đơn hàng
                LocalDate orderDate = order.getOrderDate().toLocalDate();
                for (int i = 5; i >= 0; i--) {
                    LocalDate weekStart = now.minusWeeks(i);
                    LocalDate weekEnd = weekStart.plusDays(6);

                    if (!orderDate.isBefore(weekStart) && !orderDate.isAfter(weekEnd)) {
                        String weekLabel = weekStart.format(formatter) + " - " + weekEnd.format(formatter);
                        revenueByWeek.put(weekLabel, revenueByWeek.get(weekLabel).add(orderRevenue));
                        break;
                    }
                }
            }
        }

        return revenueByWeek;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getOrderStats(Long sellerId) {
        Map<String, Integer> orderStats = new HashMap<>();

        // Đếm số lượng đơn hàng theo trạng thái
        List<Order> sellerOrders = orderRepository.findByUserId(sellerId);

        int pendingCount = 0;
        int confirmedCount = 0;
        int shippedCount = 0;
        int deliveredCount = 0;
        int cancelledCount = 0;

        for (Order order : sellerOrders) {
            switch (order.getOrderStatus()) {
                case PENDING:
                    pendingCount++;
                    break;
                case CONFIRMED:
                    confirmedCount++;
                    break;
                case SHIPPED:
                    shippedCount++;
                    break;
                case DELIVERED:
                    deliveredCount++;
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        orderStats.put("pending", pendingCount);
        orderStats.put("confirmed", confirmedCount);
        orderStats.put("shipped", shippedCount);
        orderStats.put("delivered", deliveredCount);
        orderStats.put("cancelled", cancelledCount);
        orderStats.put("total", sellerOrders.size());

        return orderStats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getProductStats(Long sellerId) {
        Map<String, Integer> productStats = new HashMap<>();

        // Đếm tổng số sản phẩm và trạng thái tồn kho
        List<Product> sellerProducts = productRepository.findBySellerId(sellerId);

        int inStock = 0;
        int outOfStock = 0;
        int lowStock = 0;  // Dưới 5 sản phẩm

        for (Product product : sellerProducts) {
            if (product.getQuantity() <= 0) {
                outOfStock++;
            } else if (product.getQuantity() < 5) {
                lowStock++;
            } else {
                inStock++;
            }
        }

        productStats.put("total", sellerProducts.size());
        productStats.put("inStock", inStock);
        productStats.put("outOfStock", outOfStock);
        productStats.put("lowStock", lowStock);

        return productStats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailyRevenue(Long sellerId) {
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();

        // Lấy dữ liệu 30 ngày gần nhất
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Khởi tạo map với các ngày và giá trị 0
        for (int i = 29; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            String dayLabel = day.format(formatter);
            revenueByDay.put(dayLabel, BigDecimal.ZERO);
        }

        // Lấy đơn hàng từ 30 ngày trước
        LocalDateTime startDate = now.minusDays(29).atStartOfDay();
        LocalDateTime endDate = now.atTime(23, 59, 59);
        List<Order> allOrders = orderRepository.findByOrderDateBetweenAndOrderStatus(
                startDate, endDate, OrderStatus.DELIVERED);

        // Lọc đơn hàng có sản phẩm của seller
        for (Order order : allOrders) {
            // Kiểm tra xem đơn hàng có chứa sản phẩm của seller hay không
            boolean hasSellersProduct = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getSellerId().equals(sellerId));

            if (hasSellersProduct) {
                // Tính doanh thu chỉ từ sản phẩm của seller trong đơn hàng này
                BigDecimal orderRevenue = order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getSellerId().equals(sellerId))
                        .map(item -> BigDecimal.valueOf(item.getPrice() * item.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Cộng dồn vào ngày tương ứng
                String dayKey = order.getOrderDate().format(formatter);
                if (revenueByDay.containsKey(dayKey)) {
                    revenueByDay.put(dayKey, revenueByDay.get(dayKey).add(orderRevenue));
                }
            }
        }

        return revenueByDay;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryRevenue(Long sellerId) {
        Map<String, BigDecimal> revenueByCategory = new LinkedHashMap<>();

        // Get delivered orders for this specific seller
        List<Order> sellerDeliveredOrders = orderRepository.findBySellerIdAndOrderStatus(sellerId, OrderStatus.DELIVERED);

        for (Order order : sellerDeliveredOrders) {
            for (OrderItem item : order.getOrderItems()) {
                // Get category name (prefer top-level category)
                String categoryName;
                if (item.getProduct().getCategory() != null) {
                    Category category = item.getProduct().getCategory();
                    if (category.getLevel() == 2 && category.getParentCategory() != null) {
                        categoryName = category.getParentCategory().getName();
                    } else {
                        categoryName = category.getName();
                    }
                } else {
                    categoryName = "Chưa phân loại";
                }

                // Use discounted price instead of original price
                BigDecimal itemRevenue = BigDecimal.valueOf(item.getDiscountedPrice() * item.getQuantity());

                revenueByCategory.merge(categoryName, itemRevenue, BigDecimal::add);
            }
        }

        // Sort by revenue descending
        return revenueByCategory.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // helper methods
    @Transactional(readOnly = true)
    private BigDecimal calculateTotalRevenue(Long sellerId) {
        List<Order> sellerDeliveredOrders = orderRepository.findBySellerIdAndOrderStatus(sellerId, OrderStatus.DELIVERED);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        totalRevenue = sellerDeliveredOrders.stream()
                .map(Order::getTotalDiscountedPrice)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRevenue;
    }

    @Transactional(readOnly = true)
    private Integer countTotalOrders(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerId(sellerId);
        return orders.size();
    }

    @Transactional(readOnly = true)
    private Integer countTotalProducts(Long sellerId) {
        return productRepository.findBySellerId(sellerId).size();
    }

    @Transactional(readOnly = true)
    private List<OrderStatsDTO> getRecentOrders(Long sellerId) {
        List<Order> orders = orderRepository.findByUserId(sellerId);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .map(order -> new OrderStatsDTO(
                        order.getId(),
                        order.getUser().getFirstName() + " " + order.getUser().getLastName(),
                        order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        order.getTotalDiscountedPrice(),
                        order.getOrderStatus().name()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    private Map<String, BigDecimal> getRevenueByMonth(Long sellerId) {
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();

        // Lấy dữ liệu 6 tháng gần nhất
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        // Khởi tạo map với các tháng và giá trị 0
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthLabel = month.format(formatter);
            revenueByMonth.put(monthLabel, BigDecimal.ZERO);
        }

        // Lấy đơn hàng từ 6 tháng trước
        LocalDateTime startDate = now.minusMonths(5).withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate = now.atTime(23, 59, 59);
        List<Order> allOrders = orderRepository.findByOrderDateBetweenAndOrderStatus(
                startDate, endDate, OrderStatus.DELIVERED);

        // Lọc đơn hàng có sản phẩm của seller
        for (Order order : allOrders) {
            // Kiểm tra xem đơn hàng có chứa sản phẩm của seller hay không
            boolean hasSellersProduct = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProduct().getSellerId().equals(sellerId));

            if (hasSellersProduct) {
                // Tính doanh thu chỉ từ sản phẩm của seller trong đơn hàng này
                BigDecimal orderRevenue = order.getOrderItems().stream()
                        .filter(item -> item.getProduct().getSellerId().equals(sellerId))
                        .map(item -> BigDecimal.valueOf(item.getPrice() * item.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Cộng dồn vào tháng tương ứng
                String monthKey = order.getOrderDate().format(formatter);
                if (revenueByMonth.containsKey(monthKey)) {
                    revenueByMonth.put(monthKey, revenueByMonth.get(monthKey).add(orderRevenue));
                }
            }
        }

        return revenueByMonth;
    }
    @Transactional(readOnly = true)
    private Integer countTotalCustomers(Long sellerId) {
        // Get all orders associated with this seller
        List<Order> sellerOrders = orderRepository.findBySellerId(sellerId);

        // Extract unique customer IDs using a Set
        Set<Long> uniqueCustomerIds = sellerOrders.stream()
                .map(order -> order.getUser().getId())
                .collect(Collectors.toSet());

        // Return the count of unique customers
        return uniqueCustomerIds.size();
    }

}