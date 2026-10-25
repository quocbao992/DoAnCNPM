package vn.doancnpm.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // Cho phép null vì có thể dòng log này chỉ dành cho Topping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Id_P", nullable = true)
    private Product product;

    // Cho phép null vì có thể dòng log này chỉ dành cho Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Id_T", nullable = true)
    private Topping topping;

    // Cho phép null khi nhập/xuất kho thủ công (không thông qua đơn hàng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Id_O", nullable = true)
    private Order order;

    @Column(name = "QuantityChange", nullable = false)
    private int quantityChange;

    @Column(name = "ChangeType", nullable = false)
    private String changeType;

    @Column(name = "createdAt", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}