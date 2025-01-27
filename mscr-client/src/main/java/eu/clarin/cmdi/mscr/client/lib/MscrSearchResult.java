package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * {
  "took": 1,
  "timed_out": false,
  "_shards": {
    "failed": 0,
    "successful": 2,
    "total": 2,
    "skipped": 1
  },
  "hits": {
    "total": {
      "relation": "eq",
      "value": 0
    },
    "hits": []
  },
  "aggregations": {
    "sterms#isReferenced": {
      "buckets": [],
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0
    },
    "sterms#organization": {
      "buckets": [],
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0
    },
    "sterms#format": {
      "buckets": [],
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0
    },
    "sterms#state": {
      "buckets": [],
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0
    },
    "sterms#type": {
      "buckets": [],
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 0
    }
  }
}
* 
 * @author wilelb
 * @param <T>
 */
public class MscrSearchResult<T> {
    @JsonProperty(value = "took")
    public int took;
    @JsonProperty(value = "timed_out")
    public boolean timed_out;
    @JsonProperty(value = "_shards")
    public Shards shards;
    @JsonProperty(value = "hits")
    public Hits<T> hits;
    
    public static class Shards {
        @JsonProperty(value = "failed")
        public int failed;
        @JsonProperty(value = "successful")
        public int successful;
        @JsonProperty(value = "total")
        public int total;
        @JsonProperty(value = "skipped")
        public int skipped;
    }
    
    public static class Hits<T> {
        @JsonProperty(value = "total")
        public HitsTotal total;
        @JsonProperty(value = "hits")
        public List<T> hits;
    }
    
    public static class HitsTotal {
        @JsonProperty(value = "relation")
        public String relation;
        @JsonProperty(value = "value")
        public int value;
    }
}