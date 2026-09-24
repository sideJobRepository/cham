package com.cham.collector.service;

import java.util.ArrayList;
import java.util.List;

/** 기관 하나를 돈 결과. 작업 로그와 건수에 더해진다 */
public class SourceResult {

    int postsSeen;
    int filesNew;
    int filesSkipped;
    int filesFailed;
    final List<String> lines = new ArrayList<>();
    String error;       // 기관 전체가 실패한 이유 (목록을 못 읽음 등)

    public int postsSeen() { return postsSeen; }
    public int filesNew() { return filesNew; }
    public int filesSkipped() { return filesSkipped; }
    public int filesFailed() { return filesFailed; }
    public List<String> lines() { return lines; }
    public String error() { return error; }

    public boolean failed() {
        return error != null;
    }

    void log(String line) {
        lines.add(line);
    }
}
