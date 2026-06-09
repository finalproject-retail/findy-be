package com.princesses7.findy.shopping.external.haccp.support;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HaccpCompanyNameParser {

	private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

	private static final Pattern EXPLICIT_DELIMITER_PATTERN = Pattern.compile("\\s*[/_:：]\\s*");

	private static final Pattern TRAILING_SEPARATOR_PATTERN = Pattern.compile("[\\s,/_:：]+$");

	private static final Pattern ADDRESS_START_PATTERN = Pattern.compile(
		"(서울특별시|서울시|부산광역시|부산시|대구광역시|대구시|인천광역시|인천시|광주광역시|광주시|대전광역시|대전시|울산광역시|울산시|세종특별자치시|세종시|경기도|강원도|충청북도|충북|충청남도|충남|전라북도|전북|전라남도|전남|경상북도|경북|경상남도|경남|제주특별자치도|제주도)\\s+[가-힣0-9]+(시|군|구|읍|면|동|로|길|번길)"
	);

	private static final Pattern REGION_ONLY_PATTERN = Pattern.compile(
		"(서울특별시|서울시|부산광역시|부산시|대구광역시|대구시|인천광역시|인천시|광주광역시|광주시|대전광역시|대전시|울산광역시|울산시|세종특별자치시|세종시|경기도|강원도|충청북도|충북|충청남도|충남|전라북도|전북|전라남도|전남|경상북도|경북|경상남도|경남|제주특별자치도|제주도)(\\s|$)"
	);

	private static final Pattern ROAD_ADDRESS_PATTERN = Pattern.compile(
		"[가-힣0-9]+(대로|로|길|번길)\\s*\\d+"
	);

	private HaccpCompanyNameParser() {
	}

	public static String extractBrandName(String seller, String manufacture) {
		String sellerName = extractCompanyName(seller);

		if (sellerName != null) {
			return normalizeBrandName(sellerName);
		}

		String manufactureName = extractCompanyName(manufacture);

		if (manufactureName != null) {
			return normalizeBrandName(manufactureName);
		}

		return null;
	}

	public static String extractCompanyName(String value) {
		if (isBlankOrUnknown(value)) {
			return null;
		}

		String normalized = normalizeText(value);

		String companyName = splitCompanyNameByExplicitDelimiter(normalized);

		if (isBlankOrUnknown(companyName)) {
			return null;
		}

		companyName = removeAddressPart(companyName);
		companyName = removeTrailingSeparators(companyName);

		if (isBlankOrUnknown(companyName)) {
			return null;
		}

		return companyName;
	}

	public static String extractAddress(String value) {
		if (isBlankOrUnknown(value)) {
			return null;
		}

		String normalized = normalizeText(value);

		String address = extractAddressByExplicitDelimiter(normalized);

		if (address != null) {
			return address;
		}

		return extractAddressByAddressPattern(normalized);
	}

	private static String splitCompanyNameByExplicitDelimiter(String value) {
		String[] parts = EXPLICIT_DELIMITER_PATTERN.split(value, 2);

		if (parts.length < 1) {
			return value;
		}

		return parts[0].trim();
	}

	private static String extractAddressByExplicitDelimiter(String value) {
		String[] parts = EXPLICIT_DELIMITER_PATTERN.split(value, 2);

		if (parts.length < 2) {
			return null;
		}

		String address = parts[1].trim();

		if (isBlankOrUnknown(address)) {
			return null;
		}

		return address;
	}

	private static String removeAddressPart(String value) {
		String normalized = normalizeText(value);
		String[] tokens = normalized.split(" ");

		for (int i = 1; i < tokens.length; i++) {
			String candidateAddress = String.join(
				" ",
				Arrays.copyOfRange(tokens, i, tokens.length)
			);

			if (looksLikeAddress(candidateAddress)) {
				return String.join(
					" ",
					Arrays.copyOfRange(tokens, 0, i)
				).trim();
			}
		}

		return normalized;
	}

	private static String extractAddressByAddressPattern(String value) {
		Matcher addressStartMatcher = ADDRESS_START_PATTERN.matcher(value);

		if (addressStartMatcher.find()) {
			return value.substring(addressStartMatcher.start()).trim();
		}

		Matcher roadAddressMatcher = ROAD_ADDRESS_PATTERN.matcher(value);

		if (roadAddressMatcher.find()) {
			return value.substring(roadAddressMatcher.start()).trim();
		}

		Matcher regionOnlyMatcher = REGION_ONLY_PATTERN.matcher(value);

		if (regionOnlyMatcher.find()) {
			return value.substring(regionOnlyMatcher.start()).trim();
		}

		return null;
	}

	private static boolean looksLikeAddress(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}

		String normalized = normalizeText(value);

		return ADDRESS_START_PATTERN.matcher(normalized).find()
			|| ROAD_ADDRESS_PATTERN.matcher(normalized).find()
			|| REGION_ONLY_PATTERN.matcher(normalized).find();
	}

	private static String normalizeBrandName(String companyName) {
		String normalized = normalizeText(companyName)
			.replace("주식회사", "")
			.replace("(주)", "")
			.replace("㈜", "")
			.replace("（주）", "")
			.replace("(유)", "")
			.replace("유한회사", "")
			.trim();

		normalized = removeTrailingSeparators(normalized);

		if (isBlankOrUnknown(normalized)) {
			return null;
		}

		return normalized;
	}

	private static String removeTrailingSeparators(String value) {
		if (value == null) {
			return null;
		}

		return TRAILING_SEPARATOR_PATTERN.matcher(value.trim())
			.replaceAll("")
			.trim();
	}

	private static String normalizeText(String value) {
		return MULTIPLE_SPACES.matcher(value.trim()).replaceAll(" ");
	}

	private static boolean isBlankOrUnknown(String value) {
		if (value == null || value.isBlank()) {
			return true;
		}

		String trimmed = value.trim();

		return trimmed.equals("_")
			|| trimmed.equals("-")
			|| trimmed.equals(".")
			|| trimmed.equals(":")
			|| trimmed.equals("：")
			|| trimmed.equals("알수없음")
			|| trimmed.equals("알 수 없음")
			|| "UNKNOWN".equalsIgnoreCase(trimmed);
	}
}