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
package eu.clarin.cmdi.virtualcollectionregistry.model.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class ParserConfigImpl implements ParserConfig {
    
    @Value("${eu.clarin.cmdi.vcr.parser.dtr.api_url:https://typeregistry.lab.pidconsortium.net}")
    private String dtrApiUrl;
    
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.api_url:https://mscr-test.2.rahtiapp.fi/datamodel-api/v2/}")
    private String mscrApiUrl;
     
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.target_schema_query:CLARIN Dublin Core}")
    private String targetSchemaQuery;

    @Value("${eu.clarin.cmdi.vcr.parser.mscr.connection_request_timeout:1000}")
    private Integer connectionRequestTimeout;
    
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.max_redirects:5}")
    private Integer maxRedirects;
    
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.transformer_factory:net.sf.saxon.TransformerFactoryImpl}")
    private String transformerFactory;
    
    @Value("${eu.clarin.cmdi.vcr.parser.cmdi.enabled:true}")
    private boolean cmdiParserEnabled;
    
    @Value("${eu.clarin.cmdi.vcr.parser.dog.enabled:true}")
    private boolean dogParserEnabled;
    
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.enabled:true}")
    private boolean mscrParserEnable;
    
    @Value("${eu.clarin.cmdi.vcr.parser.mscr.enabled:true}")
    private boolean mscrParserWithDtrExtendedTypesEnabled;
    
    @Override
    public String getDtrApiUrl() {
        return dtrApiUrl;
    }
    
    @Override
    public String getMscrApiUrl() {
        return mscrApiUrl;
    }

    @Override
    public String getTargetSchemaQuery() {
        return targetSchemaQuery;
    }

    @Override
    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    @Override
    public Integer getMaxRedirects() {
        return maxRedirects;
    }

    @Override
    public String getTransformerFactory() {
        return transformerFactory;
    }
    
    @Override
    public boolean isCmdiParserEnabled() {
        return cmdiParserEnabled;
    }
    
    @Override
    public boolean isDogParserEnabled() {
        return dogParserEnabled;
    }
    
    @Override
    public boolean isMscrParserEnabled() {
        return mscrParserEnable;
    }

    @Override
    public boolean isMscrParserWithDtrExtendedTypesEnabled() {
        return mscrParserWithDtrExtendedTypesEnabled;
    }
}
