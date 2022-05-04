package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginStatus {
    private boolean login;

    private Integer userid;

    private String username;

    private Role role;
}
