package eu.clarin.cmdi.virtualcollectionregistry.pid;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface PidRequest {
    public String toJsonString() throws JsonProcessingException;
}
