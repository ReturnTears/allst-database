package com.allst.boot.order.dto;

import com.allst.boot.order.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderView {
    private Long id;
    private Long customerId;
    private String orderNo;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String remark;
    private String receiver;
    private String phone;
    private String addressDetail;
    private LocalDateTime createdAt;
    private List<OrderItemView> items = new ArrayList<OrderItemView>();

    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}
