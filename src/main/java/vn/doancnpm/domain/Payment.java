package vn.doancnpm.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "payment")
@AllArgsConstructor 
@NoArgsConstructor 
@Getter 
@Setter 
public class Payment {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;
    
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method",nullable = false)
    private Method method;

    @Column(name = "idempotency_key",nullable = false,unique = true)
    private String idempotemcyKey;


    @Column(name = "transaction_reference",nullable = false)
    private String transactionRefer;
    
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "paid_at",nullable = false)
    private  LocalDateTime paidAt;
    public enum Method{
        VNPay,
        Momo,
        Stripe
}

    
}
