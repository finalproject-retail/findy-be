package com.princesses7.findy.user.security;

import java.time.Instant;

/**
 * 로그아웃 등으로 무효화한 액세스 토큰(jti)을 서버에서 거절하기 위한 저장소.
 */
public interface TokenBlacklist {

	boolean isBlacklisted(String jti);

	void blacklistUntil(String jti, Instant expiry);
}
