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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScanLog;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScanLogKV;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ReferenceScanDetails extends Panel {
    
    private final static Logger logger = LoggerFactory.getLogger(ReferenceScanDetails.class);
    
    public class LabelValuePanel extends Panel {    
        public LabelValuePanel(Properties props, String id, String value) {
            super(id);
            add(new Label("label", Model.of(props.getProperty(id)+":")));
            add(new Label("value", Model.of(value)));
            setVisible(value != null);
        }
    }
    
    public class KeyValuePanel extends Panel {    
        public KeyValuePanel(String id, ResourceScanLogKV kv) {
            super(id);
            add(new Label("label", Model.of(kv.getKey())));
            add(new Label("value", Model.of(kv.getValue())));
        }
    }
    
    public class ProcessorPanel extends Panel {
        public ProcessorPanel(String id, ResourceScanLog log) {
            super(id);
            add(new Label("label", Model.of(log.getProcessorId())));
            add(new Label("started", Model.of(log.getStart())));
            add(new Label("duration", Model.of(log.getEnd().getTime()-log.getStart().getTime())));
            
            final List<ResourceScanLogKV> kvs = new LinkedList<>();
            for(ResourceScanLogKV kv : log.getKvs()) {
                kvs.add(kv);
            }
            
            ListView<ResourceScanLogKV> listKvs = new ListView("listKvs", kvs) {
                @Override
                protected void populateItem(ListItem item) {
                    item.add(new KeyValuePanel("pnlKv", (ResourceScanLogKV)item.getModel().getObject()));
                }
            };
            add(listKvs);
        }
    } 
         
    public ReferenceScanDetails(String id, ResourceScan scan, ReferencesEditor.RescanHandler rescanHandler) {
        super(id);
        
        Properties props = new Properties();
        props.put("scan.details.lastscan", "Last scan");
        props.put("scan.details.lastscan.format", "YYYY-MM-dd hh:mm:ss");
        props.put("scan.details.response", "HTTP response");
        props.put("scan.details.exception", "Exception");
        props.put("scan.details.state", "State");
        
        SimpleDateFormat sdf = new SimpleDateFormat(props.getProperty("scan.details.lastscan.format"));
        
        AjaxFallbackLink<AjaxRequestTarget> reasonBtnRescan = new AjaxFallbackLink<>("btn_rescan") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                rescanHandler.rescan(scan.getRef(), target.isPresent() ? target.get() : null);
            }
        };
        add(reasonBtnRescan);
        
        add(new LabelValuePanel(props, "scan.details.lastscan", 
                scan.getLastScanEnd() != null ? 
                    sdf.format(scan.getLastScanEnd()) : "-"));
        add(new LabelValuePanel(props, "scan.details.response", 
                    scan.hasHttpResponseMessage() ? 
                        scan.getHttpResponseMessage(): "-"));
        add(new LabelValuePanel(props, "scan.details.exception", scan.getException()));
        add(new LabelValuePanel(props, "scan.details.state", scan.getState().toString()));
        
        
        List<ResourceScanLog> processors = new LinkedList<>();        
        if(scan.getLogs() != null) {
            Map<String, ResourceScanLog> processorMap = new HashMap<>();
            for(ResourceScanLog log : scan.getLogs()) {
                if(!processorMap.containsKey(log.getProcessorId())) {
                    processorMap.put(log.getProcessorId(), log);
                } else {
                    //Replace with a more recent scan log
                    ResourceScanLog existing = processorMap.get(log.getProcessorId());
                    if(log.getStart().getTime() > existing.getStart().getTime()) {
                        processorMap.put(log.getProcessorId(), log);
                    }
                }
            }
            
            //Convert processors from map into a list for UI rendering
            for(String key : processorMap.keySet()) {
                processors.add(processorMap.get(key));
            }
        }
        
         ListView<ResourceScanLog> listExamples = new ListView("listProcessors", processors) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new ProcessorPanel("pnlProcessor", (ResourceScanLog)item.getModel().getObject()));
            }
        };
        add(listExamples);
        
    }
}
