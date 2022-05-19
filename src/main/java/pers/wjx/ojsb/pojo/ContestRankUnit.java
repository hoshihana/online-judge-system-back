package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRankUnit {
    private Integer problemNumber;

    private Integer submit;

    private Integer accept;

    private Integer attempt;

    private Long timeCost;
}
