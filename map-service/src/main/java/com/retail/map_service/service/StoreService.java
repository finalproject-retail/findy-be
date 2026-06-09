package com.retail.map_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retail.map_service.dto.response.StoreResponse;
import com.retail.map_service.entity.StoreEntity;
import com.retail.map_service.entity.StoreStatus;
import com.retail.map_service.global.exception.BaseException;
import com.retail.map_service.global.exception.ErrorCode;
import com.retail.map_service.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;

	@Transactional(readOnly = true)
	public List<StoreResponse> getActiveStores() {
		return storeRepository.findByStatusOrderByStoreIdAsc(StoreStatus.ACTIVE).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public StoreResponse getStore(Long storeId) {
		StoreEntity store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BaseException(ErrorCode.STORE_NOT_FOUND));
		return toResponse(store);
	}

	private StoreResponse toResponse(StoreEntity store) {
		return new StoreResponse(
				store.getStoreId(),
				store.getStoreName(),
				store.getAddress(),
				store.getStatus().name()
		);
	}
}
