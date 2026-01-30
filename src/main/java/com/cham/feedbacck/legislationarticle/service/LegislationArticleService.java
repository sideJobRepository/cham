package com.cham.feedbacck.legislationarticle.service;

import org.springframework.web.multipart.MultipartFile;

public interface LegislationArticleService {

    void insertExcel(MultipartFile excel);
}
