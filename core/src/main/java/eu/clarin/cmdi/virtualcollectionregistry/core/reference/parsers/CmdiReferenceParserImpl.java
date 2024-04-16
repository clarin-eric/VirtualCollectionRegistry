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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public class CmdiReferenceParserImpl implements ReferenceParser {
    private final static String PARSER_ID = "PARSER_CMDI";
    
    private final Logger logger = LoggerFactory.getLogger(CmdiReferenceParserImpl.class);
    
    private ReferenceParserResult result = new ReferenceParserResult();

    @Override
    public ReferenceParserResult getResult() {
        return result;
    }
    
    @Override
    public String getId() {
        return PARSER_ID;
    }

    @Override
    public boolean parse(final String xml, final String mimeType) throws Exception {
        result = new ReferenceParserResult();
        boolean handled = false;
        if(mimeType.equalsIgnoreCase("application/x-cmdi+xml")) {
            parseCmdi(xml);
            handled = true;
        } else if(mimeType.equalsIgnoreCase("text/xml") && xml.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
            parseCmdi(xml);
            handled = true;
        }
        return handled;
    }

    private void parseCmdi(final String xml) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        logger.trace("Parsing CMDI");

        XmlParser xmlParser = new XmlParser(xml, "http://www.clarin.eu/cmd/");
        
        String profile = xmlParser.getValueForXPath( "//default:CMD/default:Header/default:MdProfile/text()");
        logger.trace("CMDI profile = " + profile);

        String name = xmlParser.getValueForXPath("//default:CMD/default:Components/default:lat-session/default:Name/text()");
        String description = xmlParser.getValueForXPath("//default:CMD/default:Components/default:lat-session/default:descriptions/default:Description[lang('eng')]/text()");
        logger.trace("Name = " + name + ", description = " + description);

        this.result.add(ReferenceParserResult.KEY_NAME, name);
        this.result.add(ReferenceParserResult.KEY_DESCRIPTION, description);
    }
}