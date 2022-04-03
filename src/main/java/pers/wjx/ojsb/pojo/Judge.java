package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Judge {
    private Integer recordId;

    private Integer testId;

    private String judgeToken;

    private JudgeResult judgeResult;

    private Integer executeTime;    // ms

    private Integer executeMemory;  // KB
}
