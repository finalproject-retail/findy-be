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
 */
@Component
public class KcaProductPriceXmlParser {

	private static final String SUCCESS_CODE = "00";
	private static final String PRICE_ITEM_TAG = "iros.openapi.service.vo.goodPriceVO";

	public KcaProductPriceResponse parse(String xml) {
		if (xml == null || xml.isBlank()) {
			return new KcaProductPriceResponse(null, "empty response", List.of());
		}

		try {
			DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();

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
			return new KcaProductPriceResponse(
				null,
				"xml parse failed: " + exception.getMessage(),
				List.of()
			);
		}
	}

	public boolean isSuccess(KcaProductPriceResponse response) {
		return response != null && SUCCESS_CODE.equals(response.resultCode());
	}

	private List<KcaProductPriceItemResponse> parseItems(Document document) {
		NodeList itemNodes = document.getElementsByTagName(PRICE_ITEM_TAG);
		if (itemNodes.getLength() == 0) {
			itemNodes = findPriceItemNodes(document);
		}

		List<KcaProductPriceItemResponse> items = new ArrayList<>();

		for (int index = 0; index < itemNodes.getLength(); index++) {
			Node itemNode = itemNodes.item(index);

			items.add(new KcaProductPriceItemResponse(
				getText(itemNode, "goodInspectDay"),
				getText(itemNode, "goodId"),
				null,
				getText(itemNode, "entpId"),
				null,
				null,
				null,
				getText(itemNode, "goodPrice"),
				null,
				null,
				null,
				null,
				null,
				getText(itemNode, "inputDttm")
			));
		}

		return items;
	}

	private NodeList findPriceItemNodes(Document document) {
		NodeList nodes = document.getElementsByTagName("*");
		List<Node> priceItemNodes = new ArrayList<>();

		for (int index = 0; index < nodes.getLength(); index++) {
			Node node = nodes.item(index);

			if (hasDirectChild(node, "goodInspectDay")
				&& hasDirectChild(node, "goodId")
				&& hasDirectChild(node, "goodPrice")) {
				priceItemNodes.add(node);
			}
		}

		return new NodeList() {
			@Override
			public Node item(int index) {
				return priceItemNodes.get(index);
			}

			@Override
			public int getLength() {
				return priceItemNodes.size();
			}
		};
	}

	private boolean hasDirectChild(Node parentNode, String tagName) {
		return getText(parentNode, tagName) != null;
	}

	private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory;
	}

	private String getText(Document document, String tagName) {
		NodeList nodes = document.getElementsByTagName(tagName);

		if (nodes.getLength() > 0 && nodes.item(0) != null) {
			return nodes.item(0).getTextContent();
		}

		NodeList allNodes = document.getElementsByTagName("*");

		for (int index = 0; index < allNodes.getLength(); index++) {
			Node node = allNodes.item(index);

			if (tagName.equalsIgnoreCase(node.getNodeName())) {
				return node.getTextContent();
			}
		}

		return null;
	}

	private String getText(Node parentNode, String tagName) {
		NodeList childNodes = parentNode.getChildNodes();

		for (int index = 0; index < childNodes.getLength(); index++) {
			Node childNode = childNodes.item(index);

			if (tagName.equalsIgnoreCase(childNode.getNodeName())) {
				return childNode.getTextContent();
			}
		}

		return null;
	}
}
