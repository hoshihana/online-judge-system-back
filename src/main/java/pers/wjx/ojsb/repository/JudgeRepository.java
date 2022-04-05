package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Judge;

import java.util.ArrayList;

@Repository
@Mapper
public interface JudgeRepository {

    boolean addJudge(Integer recordId, Integer testId);

    boolean deleteJudgesByRecordId(Integer recordId);

    ArrayList<Judge> getJudgesByRecordId(Integer recordId);

    boolean updateJudge(Judge judge);
}
