package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRank {
    private Integer contestId;

    private Integer problemAmount;

    private Integer participantAmount;

    private ArrayList<ContestRankHeaderUnit> headerUnits;

    private ArrayList<ContestRankEntry> entries;
}
