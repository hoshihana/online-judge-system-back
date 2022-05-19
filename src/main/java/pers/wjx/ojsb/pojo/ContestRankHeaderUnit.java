package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRankHeaderUnit {

    private Integer problemNumber;

    private Integer submit;

    private Integer accept;

    private Integer triedParticipant;

    private Integer passedParticipant;

    private Integer first;

}
