package com.retail.apigateway.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class UserIdHeaderRequestWrapper extends HttpServletRequestWrapper {

	private final String userId;

	public UserIdHeaderRequestWrapper(HttpServletRequest request, String userId) {
		super(request);
		this.userId = userId;
	}

	@Override
	public String getHeader(String name) {
		if (GatewayAuthHeaders.USER_ID.equalsIgnoreCase(name)) {
			return userId;
		}

		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		if (GatewayAuthHeaders.USER_ID.equalsIgnoreCase(name)) {
			return Collections.enumeration(List.of(userId));
		}

		return super.getHeaders(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		Set<String> headerNames = new HashSet<>();
		Enumeration<String> existingNames = super.getHeaderNames();
		while (existingNames.hasMoreElements()) {
			headerNames.add(existingNames.nextElement());
		}
		headerNames.add(GatewayAuthHeaders.USER_ID);

		return Collections.enumeration(new ArrayList<>(headerNames));
	}
}
