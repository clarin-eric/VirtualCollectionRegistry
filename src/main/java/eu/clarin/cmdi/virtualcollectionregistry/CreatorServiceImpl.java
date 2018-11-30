/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class CreatorServiceImpl implements CreatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(CreatorServiceImpl.class);
    
    private final Set<Creator> creators;
    private boolean initialized = false;
    
    public CreatorServiceImpl() {
        creators = new HashSet<>();
        initialized = false;
    }
    
    @Override
    public synchronized void initialize(List<VirtualCollection> collections) {
        if(!initialized) {
            for(VirtualCollection vc : collections) {
                if(vc.isPublic()) { //TODO: also check for own private collections || vc.isPrivate() && vc.getOwner() == "")
                    for(Creator c: vc.getCreators()) {
                        creators.add(c);
                    }
                }
            }            
            initialized = true;
        }
    }
        
    @Override
    public Set<Creator> getCreators() {
        return this.creators;
    }    
 
    @Override
    public int getSize() {
        return this.creators.size();
    }
}
