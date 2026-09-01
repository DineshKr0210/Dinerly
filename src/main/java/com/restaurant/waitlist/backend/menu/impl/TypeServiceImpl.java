package com.restaurant.waitlist.backend.menu.impl;

import com.restaurant.waitlist.backend.menu.dao.TypeRepository;
import com.restaurant.waitlist.backend.menu.dto.TypeDTO;
import com.restaurant.waitlist.backend.menu.mapper.TypeMapper;
import com.restaurant.waitlist.backend.menu.service.TypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeServiceImpl implements TypeService {

    private final TypeRepository typeRepository;

    @Override
    @Cacheable("AllTypes")
    public List<TypeDTO> getAllTypes() {
        return typeRepository.findAll().stream()
                .map(TypeMapper::toDTO)
                .collect(Collectors.toList());
    }
}
