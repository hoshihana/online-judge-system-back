package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserProblemRepository {

    boolean increaseTryTimes(Integer userId, Integer problemId);

}
