
package me.ccrama.rssslide.FeedSearchResults;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "results",
    "hint",
    "queryType",
    "related",
    "scheme"
})
public class FeedSearchResult {

    @JsonProperty("results")
    private List<Result> results = null;
    @JsonProperty("hint")
    private String hint;
    @JsonProperty("queryType")
    private String queryType;
    @JsonProperty("related")
    private List<String> related = null;
    @JsonProperty("scheme")
    private String scheme;

    @JsonProperty("results")
    public List<Result> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<Result> results) {
        this.results = results;
    }

    @JsonProperty("hint")
    public String getHint() {
        return hint;
    }

    @JsonProperty("hint")
    public void setHint(String hint) {
        this.hint = hint;
    }

    @JsonProperty("queryType")
    public String getQueryType() {
        return queryType;
    }

    @JsonProperty("queryType")
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    @JsonProperty("related")
    public List<String> getRelated() {
        return related;
    }

    @JsonProperty("related")
    public void setRelated(List<String> related) {
        this.related = related;
    }

    @JsonProperty("scheme")
    public String getScheme() {
        return scheme;
    }

    @JsonProperty("scheme")
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

}
