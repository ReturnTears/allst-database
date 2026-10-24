package com.allst.boot.order.service;

import com.allst.boot.catalog.entity.Product;
import com.allst.boot.catalog.enums.ProductStatus;
import com.allst.boot.catalog.repo.ProductRepository;
import com.allst.boot.common.AfterCommit;
import com.allst.boot.common.ApiException;
import com.allst.boot.common.InputValidator;
import com.allst.boot.common.PageInput;
import com.allst.boot.common.PageRequests;
import com.allst.boot.common.Slice;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.customer.entity.Address;
import com.allst.boot.customer.entity.Customer;
import com.allst.boot.customer.service.CustomerService;
import com.allst.boot.order.dto.CreateOrderInput;
import com.allst.boot.order.dto.OrderItemView;
import com.allst.boot.order.dto.OrderLineInput;
import com.allst.boot.order.dto.OrderView;
import com.allst.boot.order.entity.MallOrder;
import com.allst.boot.order.entity.OrderItem;
import com.allst.boot.order.enums.OrderStatus;
import com.allst.boot.order.repo.CustomerOrderRepository;
import com.allst.boot.order.repo.OrderItemRepository;
import com.allst.boot.order.repo.OrderSpecs;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = new EnumMap<OrderStatus, Set<OrderStatus>>(OrderStatus.class);

    static {
        NEXT.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        NEXT.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        NEXT.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.COMPLETED));
        NEXT.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        NEXT.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final OrderEventBus eventBus;
    private final InputValidator validator;

    public OrderService(CustomerOrderRepository orderRepository,
                        OrderItemRepository itemRepository,
                        ProductRepository productRepository,
                        CustomerService customerService,
                        OrderEventBus eventBus,
                        InputValidator validator) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
        this.eventBus = eventBus;
        this.validator = validator;
    }

    public OrderView get(Long id) {
        return toView(load(id), itemRepository.findByOrder_Id(id));
    }

    public OrderView getByNo(String orderNo) {
        MallOrder order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> ApiException.notFound("订单不存在"));
        return toView(order, itemRepository.findByOrder_Id(order.getId()));
    }

    public Slice<OrderView> page(Long customerId, OrderStatus status, PageInput page) {
        Page<MallOrder> result = orderRepository.findAll(OrderSpecs.by(customerId, status), PageRequests.of(page));
        return Slice.from(result, toViews(result.getContent()));
    }

    public Map<CustomerView, List<OrderView>> ordersOf(List<CustomerView> customers) {
        Map<CustomerView, List<OrderView>> result = new HashMap<CustomerView, List<OrderView>>();
        if (customers.isEmpty()) {
            return result;
        }
        List<Long> customerIds = new ArrayList<Long>();
        for (CustomerView customer : customers) {
            customerIds.add(customer.getId());
            result.put(customer, new ArrayList<OrderView>());
        }
        List<MallOrder> orders = orderRepository.findByCustomer_IdIn(customerIds);
        List<OrderView> views = toViews(orders);
        for (OrderView view : views) {
            CustomerView key = findCustomer(customers, view.getCustomerId());
            if (key != null) {
                result.get(key).add(view);
            }
        }
        return result;
    }

    @Transactional
    public OrderView create(CreateOrderInput input) {
        validator.validate(input);
        Set<Long> distinct = new HashSet<Long>();
        for (OrderLineInput line : input.getItems()) {
            if (!distinct.add(line.getProductId())) {
                throw ApiException.invalid("同一订单里商品不能重复，请合并数量");
            }
        }
        Customer customer = customerService.load(input.getCustomerId());
        Address address = customerService.loadAddress(input.getAddressId());
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw ApiException.invalid("收货地址不属于该客户");
        }
        List<Long> productIds = new ArrayList<Long>(distinct);
        Collections.sort(productIds);
        Map<Long, Product> products = new HashMap<Long, Product>();
        for (Product product : productRepository.findAllById(productIds)) {
            products.put(product.getId(), product);
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(nextOrderNo());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setRemark(blankToNull(input.getRemark()));
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddressDetail(address.getProvince() + address.getCity() + address.getDetail());
        order.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<OrderItem>();
        for (Long productId : productIds) {
            Product product = products.get(productId);
            if (product == null) {
                throw ApiException.notFound("商品不存在");
            }
            OrderLineInput line = findLine(input.getItems(), productId);
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw ApiException.illegal(product.getName() + "已下架，不能下单");
            }
            if (product.getStock() < line.getQuantity()) {
                throw ApiException.conflict(product.getName() + "库存不足");
            }
            product.setStock(product.getStock() - line.getQuantity());
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(line.getQuantity());
            item.setAmount(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            items.add(item);
            order.getItems().add(item);
            total = total.add(item.getAmount());
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(order);
        OrderView view = toView(order, items);
        AfterCommit.run(() -> eventBus.publish(view));
        return view;
    }

    @Transactional
    public OrderView updateStatus(Long id, OrderStatus status) {
        if (status == null) {
            throw ApiException.invalid("目标状态不能为空");
        }
        MallOrder order = load(id);
        List<OrderItem> items = itemRepository.findByOrder_Id(id);
        if (!NEXT.get(order.getStatus()).contains(status)) {
            throw ApiException.illegal("订单不能从" + order.getStatus().getLabel() + "变为" + status.getLabel());
        }
        if (status == OrderStatus.CANCELLED) {
            restoreStock(items);
        }
        order.setStatus(status);
        OrderView view = toView(order, items);
        AfterCommit.run(() -> eventBus.publish(view));
        return view;
    }

    @Transactional
    public OrderView cancel(Long id) {
        return updateStatus(id, OrderStatus.CANCELLED);
    }

    @Transactional
    public boolean delete(Long id) {
        MallOrder order = load(id);
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw ApiException.illegal("只有已取消的订单可以删除");
        }
        order.getItems().clear();
        orderRepository.delete(order);
        return true;
    }

    private void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    private List<OrderView> toViews(List<MallOrder> orders) {
        List<OrderView> views = new ArrayList<OrderView>();
        if (orders.isEmpty()) {
            return views;
        }
        List<Long> ids = new ArrayList<Long>();
        for (MallOrder order : orders) {
            ids.add(order.getId());
        }
        Map<Long, List<OrderItem>> grouped = new HashMap<Long, List<OrderItem>>();
        for (OrderItem item : itemRepository.findByOrder_IdIn(ids)) {
            Long orderId = item.getOrder().getId();
            List<OrderItem> bucket = grouped.get(orderId);
            if (bucket == null) {
                bucket = new ArrayList<OrderItem>();
                grouped.put(orderId, bucket);
            }
            bucket.add(item);
        }
        for (MallOrder order : orders) {
            List<OrderItem> items = grouped.get(order.getId());
            views.add(toView(order, items == null ? new ArrayList<OrderItem>() : items));
        }
        return views;
    }

    private OrderView toView(MallOrder order, List<OrderItem> items) {
        OrderView view = new OrderView();
        view.setId(order.getId());
        view.setCustomerId(order.getCustomer().getId());
        view.setOrderNo(order.getOrderNo());
        view.setStatus(order.getStatus());
        view.setTotalAmount(order.getTotalAmount());
        view.setRemark(order.getRemark());
        view.setReceiver(order.getReceiver());
        view.setPhone(order.getPhone());
        view.setAddressDetail(order.getAddressDetail());
        view.setCreatedAt(order.getCreatedAt());
        List<OrderItemView> itemViews = new ArrayList<OrderItemView>();
        for (OrderItem item : items) {
            itemViews.add(toItemView(item));
        }
        view.setItems(itemViews);
        return view;
    }

    private OrderItemView toItemView(OrderItem item) {
        OrderItemView view = new OrderItemView();
        view.setId(item.getId());
        view.setProductId(item.getProduct().getId());
        view.setProductName(item.getProductName());
        view.setPrice(item.getPrice());
        view.setQuantity(item.getQuantity());
        view.setAmount(item.getAmount());
        return view;
    }

    private MallOrder load(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> ApiException.notFound("订单不存在"));
    }

    private OrderLineInput findLine(List<OrderLineInput> lines, Long productId) {
        for (OrderLineInput line : lines) {
            if (productId.equals(line.getProductId())) {
                return line;
            }
        }
        throw ApiException.invalid("缺少商品明细");
    }

    private CustomerView findCustomer(List<CustomerView> customers, Long id) {
        for (CustomerView customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

    private String nextOrderNo() {
        for (int i = 0; i < 3; i++) {
            String orderNo = "NO" + LocalDateTime.now().format(ORDER_NO_TIME)
                    + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!orderRepository.findByOrderNo(orderNo).isPresent()) {
                return orderNo;
            }
        }
        throw ApiException.conflict("订单号生成失败，请重试");
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
