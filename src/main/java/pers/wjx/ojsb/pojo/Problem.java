package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Problem {

    private Integer id;

    private Integer authorId;

    private String name;

    private String description;

    private String inputFormat;

    private String outputFormat;

    private String explanation;

    private String samples;

    private Integer timeLimit;

    private Integer memoryLimit;

    private Integer visibility; //todo: 处理可见性问题

    private Integer submit;

    private Integer accept;
}
