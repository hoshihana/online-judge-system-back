package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.pojo.enumeration.ContestType;

import java.util.ArrayList;
import java.util.Date;

@Repository
@Mapper
public interface ContestRepository {
    boolean addContest(Contest contest);

    boolean updateContestDetail(Integer id, String name, ContestType type, String description, Boolean passwordSet, String password);

    boolean setContestProblemAmount(Integer id, Integer problemAmount);

    boolean setContestTime(Integer id, Date startTime, Date endTime);

    boolean setContestEndTime(Integer id, Date entTime);

    Contest getContestById(Integer id);

    ArrayList<Contest> getUserContestsByName(Integer authorId, String name, ArrayList<ContestType> types, Boolean orderByStartTimeAsc, Integer startIndex, Integer pageSize);

    Integer countUserContestsByName(Integer authorId, String name, ArrayList<ContestType> types);
}
