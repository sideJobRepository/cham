package com.cham.file.repository.query;

import com.cham.file.entity.ChamMonimapCommonFile;

import java.util.List;

public interface ChamMonimapCommonFileQueryRepository  {

    List<ChamMonimapCommonFile> findTargetIds(Long targetId);
}
