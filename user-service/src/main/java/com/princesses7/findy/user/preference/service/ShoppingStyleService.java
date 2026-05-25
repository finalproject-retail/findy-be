package com.princesses7.findy.user.preference.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.preference.dto.response.ShoppingStyleResponse;
import com.princesses7.findy.user.preference.repository.ShoppingStyleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingStyleService {

	private final ShoppingStyleRepository shoppingStyleRepository;

	public List<ShoppingStyleResponse> getShoppingStyles() {
		return shoppingStyleRepository.findAllByActiveTrue()
			.stream()
			.map(ShoppingStyleResponse::from)
			.toList();
	}
}