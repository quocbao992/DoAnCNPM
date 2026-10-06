package vn.doancnpm.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "order_item_toppings")
@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter 
public class OrderItemTopping {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa ngoại trỏ ngược về OrderItem cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem; 

    // Đã bỏ unique = true và đổi tên cột thành topping_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id", nullable = false)
    private Topping topping;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}