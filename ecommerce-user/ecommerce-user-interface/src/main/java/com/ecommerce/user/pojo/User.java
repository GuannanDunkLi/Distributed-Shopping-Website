package com.ecommerce.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotEmpty(message = "Username can not be empty")
    @Length(min=4, max=32, message = "the length of user name can only be between 4-32")
    private String username;

    @NotEmpty(message = "password can not be blank")
    @Length(min=4, max=32, message = "the length of password can only be between 4-32")
    @JsonIgnore
    private String password;

    @Email(message = "E-mail format is incorrect")
    private String email;

    private Date created;

    @JsonIgnore
    private String salt;
}
