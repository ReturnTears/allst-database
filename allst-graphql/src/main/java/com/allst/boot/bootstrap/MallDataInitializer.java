package com.allst.boot.bootstrap;

import com.allst.boot.catalog.dto.CategoryInput;
import com.allst.boot.catalog.dto.CategoryView;
import com.allst.boot.catalog.dto.ProductInput;
import com.allst.boot.catalog.dto.ProductView;
import com.allst.boot.catalog.enums.ProductStatus;
import com.allst.boot.catalog.service.CatalogService;
import com.allst.boot.customer.dto.AddressInput;
import com.allst.boot.customer.dto.AddressView;
import com.allst.boot.customer.dto.CreateCustomerInput;
import com.allst.boot.customer.dto.CustomerView;
import com.allst.boot.customer.repo.CustomerRepository;
import com.allst.boot.customer.service.CustomerService;
import com.allst.boot.order.dto.CreateOrderInput;
import com.allst.boot.order.dto.OrderLineInput;
import com.allst.boot.order.dto.OrderView;
import com.allst.boot.order.enums.OrderStatus;
import com.allst.boot.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

@Component
@Profile("!test")
public class MallDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MallDataInitializer.class);

    private final CustomerRepository customerRepository;
    private final CatalogService catalogService;
    private final CustomerService customerService;
    private final OrderService orderService;

    public MallDataInitializer(CustomerRepository customerRepository,
                               CatalogService catalogService,
                               CustomerService customerService,
                               OrderService orderService) {
        this.customerRepository = customerRepository;
        this.catalogService = catalogService;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            return;
        }
        CategoryView tea = category("茶饮", null);
        CategoryView ware = category("器物", null);
        CategoryView cups = category("杯盏", ware.getId());

        ProductView longjing = product("明前龙井", "杭州狮峰", "128.00", 40, ProductStatus.ON_SALE, tea.getId());
        product("武夷岩茶", "牛栏坑", "168.00", 18, ProductStatus.ON_SALE, tea.getId());
        product("青瓷主人杯", "龙泉", "89.00", 24, ProductStatus.ON_SALE, cups.getId());
        product("锤纹铜壶", "暂不出售", "460.00", 6, ProductStatus.OFF_SALE, ware.getId());

        CustomerView lin = customer("林夏", "linxia@mall.test", "13800001111", "林夏", "13800001111", "浙江省", "杭州市", "西湖区龙井路1号");
        customer("周衡", "zhouheng@mall.test", "13800002222", "周衡", "13800002222", "福建省", "武夷山市", "三姑路8号");
        AddressView address = customerService.defaultAddress(lin.getId());

        OrderLineInput line = new OrderLineInput();
        line.setProductId(longjing.getId());
        line.setQuantity(2);
        CreateOrderInput orderInput = new CreateOrderInput();
        orderInput.setCustomerId(lin.getId());
        orderInput.setAddressId(address.getId());
        orderInput.setRemark("请附茶针");
        orderInput.setItems(Collections.singletonList(line));
        OrderView order = orderService.create(orderInput);
        orderService.updateStatus(order.getId(), OrderStatus.PAID);
        log.info("样例数据已写入。GraphiQL: http://localhost:8089/graphiql  柜台: http://localhost:5173");
    }

    private CategoryView category(String name, Long parentId) {
        CategoryInput input = new CategoryInput();
        input.setName(name);
        input.setParentId(parentId);
        return catalogService.createCategory(input);
    }

    private ProductView product(String name, String description, String price, int stock, ProductStatus status, Long categoryId) {
        ProductInput input = new ProductInput();
        input.setName(name);
        input.setDescription(description);
        input.setPrice(new BigDecimal(price));
        input.setStock(stock);
        input.setStatus(status);
        input.setCategoryId(categoryId);
        return catalogService.createProduct(input);
    }

    private CustomerView customer(String name, String email, String phone,
                                  String receiver, String receiverPhone,
                                  String province, String city, String detail) {
        AddressInput address = new AddressInput();
        address.setReceiver(receiver);
        address.setPhone(receiverPhone);
        address.setProvince(province);
        address.setCity(city);
        address.setDetail(detail);
        address.setDefaultAddress(true);
        CreateCustomerInput input = new CreateCustomerInput();
        input.setName(name);
        input.setEmail(email);
        input.setPhone(phone);
        input.setAddress(address);
        return customerService.create(input);
    }
}
