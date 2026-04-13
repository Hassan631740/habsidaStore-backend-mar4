package com.habsida.store.service;

import com.habsida.store.entity.UserStoreAccess;
import com.habsida.store.repository.UserStoreAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MerchantStoreAccessService {

    private final UserStoreAccessRepository userStoreAccessRepository;

    @Transactional(readOnly = true)
    public List<Long> getStoreIds(Long userId) {
        return userStoreAccessRepository.findByUserId(userId).stream()
                .map(UserStoreAccess::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}