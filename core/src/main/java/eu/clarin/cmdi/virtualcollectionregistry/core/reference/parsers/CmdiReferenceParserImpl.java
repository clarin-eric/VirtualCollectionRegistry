/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public class CmdiReferenceParserImpl implements ReferenceParser {
    private final static String PARSER_ID = "PARSER_CMDI";
    
    private final Logger logger = LoggerFactory.getLogger(CmdiReferenceParserImpl.class);
    
    private final ReferenceParserResult result = new ReferenceParserResult();

    public ReferenceParserResult getResult() {
        return result;
    }
    
    @Override
    public String getId() {
        return PARSER_ID;
    }

    @Override
    public boolean parse(final String xml, final String mimeType) {
        boolean handled = false;

        try {
            if(mimeType.equalsIgnoreCase("application/x-cmdi+xml")) {
                parseCmdi(xml);
            } else if(mimeType.equalsIgnoreCase("text/xml") && xml.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                parseCmdi(xml);
            }
        } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
            logger.error("Failed to parse CMDI", ex);
        }

        return handled;
    }

    private void parseCmdi(final String xml) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        logger.trace("Parsing CMDI");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        String profile = getValueForXPath(doc, "//default:CMD/default:Header/default:MdProfile/text()");
        logger.trace("CMDI profile = " + profile);

        String name = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:Name/text()");
        String description = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:descriptions/default:Description[lang('eng')]/text()");
        logger.trace("Name = " + name + ", description = " + description);

        this.result.add(ReferenceParserResult.KEY_NAME, name);
        this.result.add(ReferenceParserResult.KEY_DESCRIPTION, description);
    }

    /**
     * Return the first value of the xpath query result, or null if the result is
     * empty
     *
     * @param doc
     * @param xpathQuery
     * @return
     */
    private String getValueForXPath(Document doc, String xpathQuery) {
        List<String> result = getValuesForXPath(doc, xpathQuery);
        if(result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * Return all values for the xpath query
     *
     * @param doc
     * @param xpathQuery
     * @return
     */
    private List<String> getValuesForXPath(Document doc, String xpathQuery) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                return prefix.equals("default") ? "http://www.clarin.eu/cmd/" : null;
            }

            @Override
            public Iterator<String> getPrefixes(String val) {
                return null;
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }
        });

        List<String> result = new ArrayList<>();

        try {
            XPathExpression expr = xpath.compile(xpathQuery);
            Object xpathResult = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) xpathResult;
            logger.trace("XPatch query = ["+xpathQuery+"], result nodelist.getLength() = "+nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                Node currentItem = nodes.item(i);
                logger.trace("found node -> " + currentItem.getLocalName() + " (namespace: " + currentItem.getNamespaceURI() + "), value = " + currentItem.getNodeValue());
                result.add(currentItem.getNodeValue());
            }
        } catch(XPathExpressionException ex) {
            logger.error("XPath query ["+xpathQuery+"] failed.", ex);
        }

        return result;
    }
}