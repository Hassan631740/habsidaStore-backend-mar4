package com.habsida.store.service;

import com.habsida.store.dto.DtoMapper;
import com.habsida.store.dto.request.*;
import com.habsida.store.dto.response.*;
import com.habsida.store.entity.*;
import com.habsida.store.exception.ResourceNotFoundException;
import com.habsida.store.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreSettingsService {

    private final StoreRepository storeRepository;
    private final StoreDeliverySettingsRepository deliverySettingsRepository;
    private final StoreDeliveryAreaRepository deliveryAreaRepository;
    private final StoreDeliveryRestrictionRepository deliveryRestrictionRepository;
    private final StoreHoursRepository storeHoursRepository;
    private final StoreBreaksRepository storeBreaksRepository;

    // ---- Delivery Settings ----

    @Transactional(readOnly = true)
    public StoreDeliverySettingsResponse getDeliverySettings(Long storeId) {
        requireStore(storeId);
        return deliverySettingsRepository.findByStoreId(storeId)
                .map(DtoMapper::toResponse)
                .orElseGet(() -> StoreDeliverySettingsResponse.builder().storeId(storeId).build());
    }

    @Transactional
    public StoreDeliverySettingsResponse upsertDeliverySettings(Long storeId, StoreDeliverySettingsRequest request) {
        requireStore(storeId);
        StoreDeliverySettings entity = deliverySettingsRepository.findByStoreId(storeId)
                .orElseGet(() -> {
                    StoreDeliverySettings s = new StoreDeliverySettings();
                    s.setStoreId(storeId);
                    return s;
                });
        entity.setDeliveryFee(request.getDeliveryFee());
        entity.setMinOrderAmount(request.getMinOrderAmount());
        return DtoMapper.toResponse(deliverySettingsRepository.save(entity));
    }

    // ---- Delivery Areas ----

    @Transactional(readOnly = true)
    public List<StoreDeliveryAreaResponse> getDeliveryAreas(Long storeId) {
        requireStore(storeId);
        return deliveryAreaRepository.findByStoreId(storeId)
                .stream().map(DtoMapper::toResponse).toList();
    }

    @Transactional
    public List<StoreDeliveryAreaResponse> replaceDeliveryAreas(Long storeId, List<StoreDeliveryAreaRequest> requests) {
        requireStore(storeId);
        deliveryAreaRepository.deleteByStoreId(storeId);
        List<StoreDeliveryArea> saved = requests.stream()
                .map(r -> {
                    StoreDeliveryArea e = DtoMapper.toEntity(r);
                    e.setStoreId(storeId);
                    return deliveryAreaRepository.save(e);
                }).toList();
        return saved.stream().map(DtoMapper::toResponse).toList();
    }

    // ---- Delivery Restrictions ----

    @Transactional(readOnly = true)
    public List<StoreDeliveryRestrictionResponse> getDeliveryRestrictions(Long storeId) {
        requireStore(storeId);
        return deliveryRestrictionRepository.findByStoreId(storeId)
                .stream().map(DtoMapper::toResponse).toList();
    }

    @Transactional
    public List<StoreDeliveryRestrictionResponse> replaceDeliveryRestrictions(Long storeId, List<StoreDeliveryRestrictionRequest> requests) {
        requireStore(storeId);
        deliveryRestrictionRepository.deleteByStoreId(storeId);
        List<StoreDeliveryRestriction> saved = requests.stream()
                .map(r -> {
                    StoreDeliveryRestriction e = DtoMapper.toEntity(r);
                    e.setStoreId(storeId);
                    return deliveryRestrictionRepository.save(e);
                }).toList();
        return saved.stream().map(DtoMapper::toResponse).toList();
    }

    // ---- Work Hours ----

    @Transactional(readOnly = true)
    public List<StoreHoursResponse> getWorkHours(Long storeId) {
        requireStore(storeId);
        return storeHoursRepository.findByStoreId(storeId)
                .stream().map(DtoMapper::toResponse).toList();
    }

    @Transactional
    public List<StoreHoursResponse> replaceWorkHours(Long storeId, List<StoreHoursRequest> requests) {
        requireStore(storeId);
        storeHoursRepository.deleteByStoreId(storeId);
        List<StoreHours> saved = requests.stream()
                .map(r -> {
                    StoreHours e = DtoMapper.toEntity(r);
                    e.setStoreId(storeId);
                    return storeHoursRepository.save(e);
                }).toList();
        return saved.stream().map(DtoMapper::toResponse).toList();
    }

    // ---- Breaks ----

    @Transactional(readOnly = true)
    public List<StoreBreaksResponse> getBreaks(Long storeId) {
        requireStore(storeId);
        return storeBreaksRepository.findByStoreId(storeId)
                .stream().map(DtoMapper::toResponse).toList();
    }

    @Transactional
    public List<StoreBreaksResponse> replaceBreaks(Long storeId, List<StoreBreaksRequest> requests) {
        requireStore(storeId);
        storeBreaksRepository.deleteByStoreId(storeId);
        List<StoreBreaks> saved = requests.stream()
                .map(r -> {
                    StoreBreaks e = DtoMapper.toEntity(r);
                    e.setStoreId(storeId);
                    return storeBreaksRepository.save(e);
                }).toList();
        return saved.stream().map(DtoMapper::toResponse).toList();
    }

    // ---- helpers ----

    private void requireStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", storeId);
        }
    }
}