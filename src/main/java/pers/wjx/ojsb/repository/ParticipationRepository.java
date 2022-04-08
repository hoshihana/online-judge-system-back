package pers.wjx.ojsb.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ParticipationRepository {
    boolean addParticipation(Integer contestId, Integer userId, String username, String nickname);
}
