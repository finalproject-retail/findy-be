package com.princesses7.findy.shopping.external.kca;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceResponse;

/*
	공공데이터포털 응답이 XML이라, RestClient로 String을 받은 뒤 직접 파싱
	jackson-dataformat-xml 의존성을 추가하지 않아도 됨
 */
@Component
public class KcaProductPriceXmlParser {

	private static final String SUCCESS_CODE = "00";

	public KcaProductPriceResponse parse(String xml) {
		if (xml == null || xml.isBlank()) {
			return new KcaProductPriceResponse(null, "empty response", List.of());
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);

			Document document = factory.newDocumentBuilder()
				.parse(new InputSource(new StringReader(xml)));

			document.getDocumentElement().normalize();

			String resultCode = getText(document, "resultCode");
			String resultMessage = getText(document, "resultMsg");

			return new KcaProductPriceResponse(
				resultCode,
				resultMessage,
				parseItems(document)
			);
		} catch (Exception exception) {
			return new KcaProductPriceResponse(null, "xml parse failed: " + exception.getMessage(), List.of());
		}
	}

	public boolean isSuccess(KcaProductPriceResponse response) {
		return response != null && SUCCESS_CODE.equals(response.resultCode());
	}

	private List<KcaProductPriceItemResponse> parseItems(Document document) {
		NodeList itemNodes = document.getElementsByTagName("iros.openapi.service.vo.goodPriceVO");
		List<KcaProductPriceItemResponse> items = new ArrayList<>();

		for (int index = 0; index < itemNodes.getLength(); index++) {
			Node itemNode = itemNodes.item(index);

			items.add(new KcaProductPriceItemResponse(
				getText(itemNode, "goodInspectDay"),
				getText(itemNode, "entpId"),
				getText(itemNode, "goodId"),
				getText(itemNode, "goodPrice"),
				getText(itemNode, "inputDttm")
			));
		}

		return items;
	}

	private String getText(Document document, String tagName) {
		NodeList nodes = document.getElementsByTagName(tagName);

		if (nodes.getLength() == 0 || nodes.item(0) == null) {
			return null;
		}

		return nodes.item(0).getTextContent();
	}

	private String getText(Node parentNode, String tagName) {
		NodeList childNodes = parentNode.getChildNodes();

		for (int index = 0; index < childNodes.getLength(); index++) {
			Node childNode = childNodes.item(index);

			if (tagName.equals(childNode.getNodeName())) {
				return childNode.getTextContent();
			}
		}

		return null;
	}
}