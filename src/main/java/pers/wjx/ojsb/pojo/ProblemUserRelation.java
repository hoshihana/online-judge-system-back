package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemUserRelation {

    private Integer userId;

    private Integer problemId;

    private Integer submit;

    private Integer accept;

    private Boolean star;

}
