package com.cham.collector.adapter;

import com.cham.collector.domain.EngineType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AdapterRegistry {

    private final Map<EngineType, BoardAdapter> adapters = new EnumMap<>(EngineType.class);

    public AdapterRegistry(List<BoardAdapter> all) {
        all.forEach(a -> adapters.put(a.engine(), a));
    }

    public Optional<BoardAdapter> find(EngineType engine) {
        return Optional.ofNullable(adapters.get(engine));
    }
}
