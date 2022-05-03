package pers.wjx.ojsb.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    private Integer id;

    private String username;

    @JsonIgnore
    private String password;

    private String email;

    private Role role;
}
