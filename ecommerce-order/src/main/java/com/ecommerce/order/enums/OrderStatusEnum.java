package com.ecommerce.order.enums;

public enum  OrderStatusEnum {
    INIT(1, "Initialized, not paid"),
    PAYED(2, "Paid, not shipped")
//    DELIVERED(3, "已发货，未确认"),
//    CONFIRMED(4, "已确认,未评价"),
//    CLOSED(5, "已关闭"),
//    RATED(6, "已评价，交易结束")
    ;

    private Integer code;
    private String msg;

    OrderStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer value(){
        return this.code;
    }

    public String msg(){
        return msg;
    }
}
