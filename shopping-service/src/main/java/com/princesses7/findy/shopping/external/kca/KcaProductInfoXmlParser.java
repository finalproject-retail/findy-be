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

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoResponse;

@Component
public class KcaProductInfoXmlParser {

	public KcaProductInfoResponse parse(String xml) {
		if (xml == null || xml.isBlank()) {
			return new KcaProductInfoResponse(null, "empty response", List.of());
		}

		try {
			DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();

			Document document = factory.newDocumentBuilder()
				.parse(new InputSource(new StringReader(xml)));

			document.getDocumentElement().normalize();

			return new KcaProductInfoResponse(
				getText(document, "resultCode"),
				getText(document, "resultMsg"),
				parseItems(document)
			);
		} catch (Exception exception) {
			return new KcaProductInfoResponse(
				null,
				"xml parse failed: " + exception.getMessage(),
				List.of()
			);
		}
	}

	private List<KcaProductInfoItemResponse> parseItems(Document document) {
		NodeList resultNodes = document.getElementsByTagName("result");
		List<KcaProductInfoItemResponse> items = new ArrayList<>();

		if (resultNodes.getLength() == 0) {
			return parseItemsByFields(document);
		}

		Node resultNode = resultNodes.item(0);
		NodeList childNodes = resultNode.getChildNodes();

		for (int index = 0; index < childNodes.getLength(); index++) {
			Node itemNode = childNodes.item(index);

			if (itemNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			String goodId = getText(itemNode, "goodId");

			if (goodId == null || goodId.isBlank()) {
				continue;
			}

			items.add(new KcaProductInfoItemResponse(
				goodId,
				getText(itemNode, "goodName"),
				getText(itemNode, "productEntpCode"),
				getText(itemNode, "productEntpName")
			));
		}

		return items;
	}

	private List<KcaProductInfoItemResponse> parseItemsByFields(Document document) {
		NodeList nodes = document.getElementsByTagName("*");
		List<KcaProductInfoItemResponse> items = new ArrayList<>();

		for (int index = 0; index < nodes.getLength(); index++) {
			Node itemNode = nodes.item(index);
			String goodId = getText(itemNode, "goodId");

			if (goodId == null || goodId.isBlank()) {
				continue;
			}

			items.add(new KcaProductInfoItemResponse(
				goodId,
				getText(itemNode, "goodName"),
				getText(itemNode, "productEntpCode"),
				getText(itemNode, "productEntpName")
			));
		}

		return items;
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
