package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {

    private Integer id;

    private Integer authorId;

    private String authorUsername;

    private String name;

    private String description;

    private String inputFormat;

    private String outputFormat;

    private String explanation;

    private String samples;

    private Integer timeLimit;  // (ms)

    private Integer memoryLimit;    // (MB)

    private Boolean testSet;

    private Visibility visibility;

    private Integer submit;

    private Integer accept;
}
