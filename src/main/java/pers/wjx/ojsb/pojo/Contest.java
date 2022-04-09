package pers.wjx.ojsb.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pers.wjx.ojsb.pojo.enumeration.ContestType;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contest {
    private Integer id;

    private Integer authorId;

    private String authorUsername;

    private String name;

    private ContestType type;

    private String description;

    private Boolean passwordSet;

    @JsonIgnore
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;
}

