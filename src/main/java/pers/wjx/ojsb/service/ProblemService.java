package pers.wjx.ojsb.service;

import pers.wjx.ojsb.pojo.ProblemBrief;

import java.util.ArrayList;

public interface ProblemService {
    ArrayList<ProblemBrief> getProblemBriefByKey(String key, Integer pageIndex, Integer pageSize);
}
