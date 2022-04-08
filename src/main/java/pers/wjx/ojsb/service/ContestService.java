package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.Contest;
import pers.wjx.ojsb.pojo.enumeration.ContestType;

import java.util.ArrayList;
import java.util.Date;

public interface ContestService {
    Integer addContest(Integer authorId, String name, ContestType type, String description, String password, Date startTime, Date endTime);

    boolean updateContestDetail(Integer id, String name, ContestType type, String description, String password);

    boolean setContestTime(Integer id, Date startTime, Date endTime);

    boolean resetContest(Integer id, Date startTime, Date endTime);

    boolean setContestEndTime(Integer id, Date endTime);

    boolean setContestProblems(Integer id, ArrayList<Integer> problemIds);

    boolean participateContest(Integer id, Integer userId, String nickname);

    Contest getContestById(Integer id);
}
