package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.ContestRankUnit;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRankEntry {
    private Integer userId;

    private String username;

    private String nickname;

    private Integer rank;

    private Long totalTimeCost;

    private Integer totalAccept;

    private ArrayList<ContestRankUnit> units;
}
