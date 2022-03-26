package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TryPassAmountPair {
    private Integer triedUserAmount;

    private Integer passedUserAmount;
}
