package com.ecommerce.order.service;

import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.utils.IdWorker;
import com.ecommerce.item.pojo.Sku;
import com.ecommerce.order.client.AddressClient;
import com.ecommerce.order.client.GoodsClient;
import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.enums.OrderStatusEnum;
import com.ecommerce.order.enums.PayState;
import com.ecommerce.order.interceptors.UserInterceptor;
import com.ecommerce.order.mapper.OrderDetailMapper;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.mapper.OrderStatusMapper;
import com.ecommerce.order.pojo.Order;
import com.ecommerce.order.pojo.OrderDetail;
import com.ecommerce.order.pojo.OrderStatus;
import com.ecommerce.order.utils.PayHelper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDto orderDto) {
        Order order = new Order();
        // 1.1 Generate order ID, using snowflake algorithm in utils
        long orderId = idWorker.nextId();

        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setPaymentType(orderDto.getPaymentType());

        // 1.2 Get user information
        UserInfo user = UserInterceptor.getLoginUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        // 1.3 The consignee address information should be obtained from the logistics information in the database.
        // The fake data is used here.
        AddressDto addressDTO = AddressClient.findById(orderDto.getAddressId());
        if (addressDTO == null) {
            throw new EException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());

        // 1.4 The payment amount is related. First, the orderDto is converted into a map, where the key is skuId,
        // and the value is the purchase quantity of the sku in the shopping cart.
        Map<Long, Integer> skuNumMap = orderDto.getCarts().stream().collect(Collectors.toMap(c -> c.getSkuId(), c -> c.getNum()));
        // Query goods information, query sku details based on skuIds
        List<Sku> skus = goodsClient.querySkuBySkuIds(new ArrayList<>(skuNumMap.keySet()));

        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * skuNumMap.get(sku.getId());

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(skuNumMap.get(sku.getId()));
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            details.add(orderDetail);
        }
        order.setActualPay((totalPay + order.getPostFee()));
        order.setTotalPay(totalPay);

        // 1.5 put the order into database
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            throw new EException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 2. Save order details
        count = orderDetailMapper.insertList(details);
        if (count != details.size()) {
            throw new EException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 3. Add order status
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(order.getCreateTime());

        // store orderStatus
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1) {
            throw new EException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 4. Decrease the number of stock
        goodsClient.decreaseStock(orderDto.getCarts());

        return orderId;

    }

    public String generateUrl(Long orderId) {
        // Query orders based on order ID
        Order order = queryById(orderId);
        // Determine order status
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            throw new EException(ExceptionEnum.ORDER_STATUS_EXCEPTION);
        }
        // Payment amount
//        Long actualPay = order.getActualPay();

        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();
        // 这里传入一份钱，用于测试使用，实际中使用订单中的实付金额
        String url = payHelper.createOrder(orderId, 1L, desc);
        return url;

    }

    public Order queryById(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new EException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        order.setOrderDetails(orderDetails);

        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        return order;
    }

    //    @Transactional
    public void handleNotify(Map<String, String> result) {
        // 1. Data validation
        payHelper.isSuccess(result);

        // 2. Verify signature
        payHelper.isValidSign(result);

        // 3. Check order money
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr)) {
            throw new EException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        Long totalFee = Long.valueOf(totalFeeStr);

        Long orderid = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderid);
        if (totalFee != /*order.getActualPay()*/ 1) {
            throw new EException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        // 4. Modify order status
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderid);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1) {
            throw new EException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
    }

    public PayState queryOrderState(Long orderId) {
        // Query order status
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();

        if (status != OrderStatusEnum.INIT.value()) {
            return PayState.SUCCESS;
        }

        // If it is not paid, it may not be unpaid,
        // it may have been paid, but the notify has not arrived, you must go to WeChat to check the payment status.
        return payHelper.queryPayState(orderId);
    }
}