package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface PidRequest {
    public String toJsonString() throws JsonProcessingException;
}
