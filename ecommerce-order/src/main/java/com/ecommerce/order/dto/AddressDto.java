package com.ecommerce.order.dto;

import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private String name;
    private String phone;
    private String state;
    private String city;
    private String district;
    private String address;
    private String  zipCode;
    private Boolean isDefault;
}
