package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.Record;

public interface JudgeService {

    void initialize();

    void sendJudge();

    void offerPendingRecord(Record pendingRecord);

    void checkJudge();
}
