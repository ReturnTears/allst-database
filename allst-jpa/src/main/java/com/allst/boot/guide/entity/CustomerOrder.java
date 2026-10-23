package com.allst.boot.guide.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单明细默认懒加载。直接循环 {@code order.getItems()} 就是经典 N+1。
 * {@link BatchSize} 把 N 次加载收成按批的 IN 查询，不能替代 fetch join。
 */
@Getter
@Setter
@Entity
@Table(name = "g_customer_order")
@NamedEntityGraph(name = "Order.withItems", attributeNodes = @NamedAttributeNode("items"))
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "customer_name", nullable = false, length = 64)
    private String customerName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 2)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(String sku, int quantity, java.math.BigDecimal price) {
        OrderItem item = new OrderItem();
        item.setSku(sku);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setOrder(this);
        items.add(item);
    }
}
