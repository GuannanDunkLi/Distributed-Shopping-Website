package com.ecommerce.order.client;

import com.ecommerce.order.dto.AddressDto;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDto> addressList = new ArrayList<AddressDto>(){
        {
            AddressDto address = new AddressDto();
            address.setId(1L);
            address.setAddress("xxxxxxxx");
            address.setCity("xx");
            address.setDistrict("xxxx");
            address.setName("xx");
            address.setPhone("15800000000");
            address.setState("xx");
            address.setZipCode("21000");
            address.setIsDefault(true);
            add(address);

            AddressDto address2 = new AddressDto();
            address2.setId(2L);
            address2.setAddress("xxxxxxxxxxxxxx");
            address2.setCity("xxx");
            address2.setDistrict("xxx");
            address2.setName("xxx");
            address2.setPhone("13600000000");
            address2.setState("xx");
            address2.setZipCode("100000");
            address2.setIsDefault(false);
            add(address2);
        }
    };

    public static AddressDto findById(Long id){
        for (AddressDto addressDTO : addressList) {
            if(addressDTO.getId() == id) return addressDTO;
        }
        return null;
    }
}