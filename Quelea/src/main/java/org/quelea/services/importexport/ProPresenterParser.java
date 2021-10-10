/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.importexport;

import org.quelea.data.displayable.SongDisplayable;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.Utils;
import org.quelea.windows.main.StatusPanel;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses ProPrsenter 4, 5 and 6 XML files.
 *
 * @author Michael
 */
public class ProPresenterParser implements SongParser {

	private static final Logger LOGGER = LoggerUtils.getLogger();
	private final Map<Integer, BiFunction<String, Node, Optional<String>>> slideTransformers;

	public ProPresenterParser() {
		this.slideTransformers=Map.of(4, this::getSectionTextLegacy,
				5, this::getSectionTextLegacy,
				6, this::getSectionText6
				);
	}

	@Override
	public List<SongDisplayable> getSongs(File file, StatusPanel statusPanel) throws IOException {
		return getSong(file).map(Collections::singletonList).orElse(Collections.emptyList());
	}

	private Optional<SongDisplayable> getSong(File file) {
		String encoding = Utils.getEncoding(file);
		int ppVersion = getVersion(file.getAbsolutePath());
		if (ppVersion < 4 || ppVersion > 6) {
			LOGGER.log(Level.WARNING, "Can only parse versions 4-6");
			return Optional.empty();
		}

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			String title = getTitle(doc.getDocumentElement());
			String author = getAuthor(doc.getDocumentElement());

			StringBuilder lyrics = new StringBuilder();

			NodeList slideList = doc.getElementsByTagName("RVDisplaySlide");
			LOGGER.log(Level.INFO, "Found {0} slides", slideList.getLength());
			for (int i = 0; i < slideList.getLength(); i++) {
				slideTransformers
						.getOrDefault(ppVersion, (s,n) -> Optional.empty())
						.apply(encoding, slideList.item(i))
						.map(this::appendNewline)
						.ifPresent(lyrics::append);
			}
			SongDisplayable song = new SongDisplayable(title, author);
			song.setLyrics(lyrics.toString().trim());
			return Optional.of(song);
		} catch (IOException | ParserConfigurationException | DOMException | SAXException ex) {
			LOGGER.log(Level.SEVERE, "Error with import", ex);
			return Optional.empty();
		}
	}

	private String getAuthor(Element rootElement) {
		String[] attrsToTry = {"artist", "CCLIAuthor", "CCLIArtistCredits"};
		return tryAttrs(rootElement, attrsToTry).orElse("");
	}

	private String getTitle(Element rootElement) {
		String[] attrsToTry = {"CCLISongTitle"};
		return tryAttrs(rootElement, attrsToTry).orElse("Unknown");
	}

	private Optional<String> tryAttrs(Element ele, String[] attrsToTry) {
		for (String attrToTry : attrsToTry) {
			String attrValue = ele.getAttribute(attrToTry);
			if (attrValue != null && !attrValue.isEmpty()) {
				return Optional.of(attrValue);
			}
		}
		return Optional.empty();
	}

	private Optional<String> getSectionTextLegacy(String encoding, Node slideNode) {
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			StringBuilder ret = new StringBuilder();
			XPathExpression expr = xPathfactory.newXPath().compile(".//RVTextElement");
			NodeList lines = (NodeList) expr.evaluate(slideNode, XPathConstants.NODESET);
			LOGGER.log(Level.INFO, "Found {0} lines", lines.getLength());
			for (int j = 0; j < lines.getLength(); j++) {
				Node lineNode = lines.item(j);
				String line = new String(Base64.getDecoder().decode(lineNode.getAttributes().getNamedItem("RTFData").getTextContent()), Charset.forName(encoding));
				line = stripRtfTags(line).trim();
				ret.append(line).append('\n');
			}
			return Optional.of(ret.toString());
		} catch (XPathExpressionException | DOMException ex) {
			LOGGER.log(Level.SEVERE, "Error with import legacy", ex);
			return Optional.empty();
		}
	}

	private Optional<String> getSectionText6(String encoding, Node slideNode) {
		try {
			XPathFactory xPathfactory = XPathFactory.newInstance();
			StringBuilder ret = new StringBuilder();
			XPathExpression expr = xPathfactory.newXPath().compile(".//NSString[@rvXMLIvarName=\"PlainText\"]");
			NodeList lines = (NodeList) expr.evaluate(slideNode, XPathConstants.NODESET);
			for (int j = 0; j < lines.getLength(); j++) {
				Node lineNode = lines.item(j);
				String line = new String(Base64.getDecoder().decode(lineNode.getTextContent()), Charset.forName(encoding));
				ret.append(line).append('\n');
			}
			return Optional.of(ret.toString());
		} catch (XPathExpressionException | DOMException ex) {
			LOGGER.log(Level.SEVERE, "Error with import v6", ex);
			return Optional.empty();
		}
	}

	private int getVersion(String filePath) {
		try {
			return Integer.parseInt(Character.toString(filePath.charAt(filePath.length() - 1)));
		} catch (NumberFormatException ex) {
			LOGGER.log(Level.SEVERE, "Can''t work out version of {0}", filePath);
			return -1;
		}
	}

	private String stripRtfTags(String text) {
		RTFEditorKit rtfParser = new RTFEditorKit();
		javax.swing.text.Document document = rtfParser.createDefaultDocument();
		try {
			rtfParser.read(new ByteArrayInputStream(text.getBytes("UTF-8")), document, 0);
			return document.getText(0, document.getLength());
		} catch (IOException | BadLocationException ex) {
			LOGGER.log(Level.SEVERE, "Error stripping RTF tags", ex);
			return text;
		}
	}

	private String appendNewline(String text) {
		return text + "\n";
	}

}
