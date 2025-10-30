package com.cham.theme.respotiroy.query;

import com.cham.theme.dto.response.ThemeGetResponse;
import com.cham.theme.entity.ChamMonimapTheme;

import java.util.List;

public interface ChamMonimapThemeQueryRepository {
    
    List<ThemeGetResponse> findByThemes();
    
    List<Long> findByDuplicationCheckTargetId(List<Long> targetIds);
    
    List<ChamMonimapTheme> findByThemeIdsIn(List<Long> ids);
    
}
