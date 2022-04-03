package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Judge;

@Repository
@Mapper
public interface JudgeRepository {

    boolean addJudge(Integer recordId, Integer testId);

    boolean deleteJudgesByRecordId(Integer recordId);

    boolean updateJudge(Judge judge);
}
