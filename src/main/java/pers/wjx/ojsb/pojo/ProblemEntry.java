package pers.wjx.ojsb.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.Visibility;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemEntry {
    private Integer id;

    private String name;

    private Visibility visibility;

    private Integer submit;

    private Integer accept;
}
