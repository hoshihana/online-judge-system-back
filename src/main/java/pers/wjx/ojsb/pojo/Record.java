package pers.wjx.ojsb.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.JudgeResult;
import pers.wjx.ojsb.pojo.enumeration.Language;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private Integer id;

    private Integer userId;

    private String username;

    private Integer problemId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date submitTime;

    private Integer codeLength; // (B)

    private Language submitLanguage;

    private JudgeResult judgeResult;

    private Integer executeTime; // (ms)

    private Integer executeMemory; // (KB)

    @JsonIgnore
    private Integer testAmount;

    @JsonIgnore
    private Integer acceptedTestAmount;

    @JsonIgnore
    private String compileOutput;
}
