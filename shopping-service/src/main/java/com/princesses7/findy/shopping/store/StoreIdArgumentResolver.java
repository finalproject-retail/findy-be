package com.princesses7.findy.shopping.store;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class StoreIdArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String STORE_ID_HEADER = "X-Store-Id";
	public static final String STORE_ID_PARAM = "storeId";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ResolvedStoreId.class);
	}

	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		String headerValue = webRequest.getHeader(STORE_ID_HEADER);
		Long storeId = parseLong(headerValue);

		if (storeId == null) {
			storeId = parseLong(webRequest.getParameter(STORE_ID_PARAM));
		}

		return StoreIdSupport.resolve(storeId);
	}

	private Long parseLong(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
