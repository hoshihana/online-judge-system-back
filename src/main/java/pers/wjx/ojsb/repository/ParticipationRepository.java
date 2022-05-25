package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import pers.wjx.ojsb.pojo.Participation;

@Repository
@Mapper
public interface ParticipationRepository {
    boolean addParticipation(Integer contestId, Integer userId, String username, String nickname);

    Integer deleteParticipationByContestId(Integer contestId);

    Participation getParticipation(Integer contestId, Integer userId);

    Integer setParticipationNickname(Integer contestId, Integer userId, String nickname);

    Integer countParticipatedContestByUserId(Integer userId);
}
