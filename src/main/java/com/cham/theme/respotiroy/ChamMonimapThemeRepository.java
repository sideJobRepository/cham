package com.cham.theme.respotiroy;

import com.cham.theme.entity.ChamMonimapTheme;
import com.cham.theme.respotiroy.query.ChamMonimapThemeQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamMonimapThemeRepository extends JpaRepository<ChamMonimapTheme, Long> , ChamMonimapThemeQueryRepository {


}
