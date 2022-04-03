package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeStatus {
    private Integer id;

    private String description;

    public JudgeResult getJudgeResult() {
        switch (id) {
            case 1:
            case 2:
                return JudgeResult.JD;
            case 3:
                return JudgeResult.AC;
            case 4:
                return JudgeResult.WA;
            case 5:
                return JudgeResult.TLE;
            case 6:
                return JudgeResult.CE;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return JudgeResult.RE;
            case 13:
            case 14:
            default:
                return JudgeResult.SE;
        }
    }
}
