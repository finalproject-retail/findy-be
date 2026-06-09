package com.princesses7.findy.shopping.store;

public final class StoreIdSupport {

	public static final long DEFAULT_STORE_ID = 1L;

	private StoreIdSupport() {
	}

	public static long resolve(long storeId) {
		return storeId > 0 ? storeId : DEFAULT_STORE_ID;
	}

	public static long resolve(Long storeId) {
		return storeId != null && storeId > 0 ? storeId : DEFAULT_STORE_ID;
	}
}
