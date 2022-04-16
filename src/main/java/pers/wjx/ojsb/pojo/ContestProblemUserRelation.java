package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestProblemUserRelation {
    private Integer contestId;

    private Integer problemId;

    private Integer userId;

    private Integer problemNumber;

    private Integer submit;

    private Integer accept;
}
