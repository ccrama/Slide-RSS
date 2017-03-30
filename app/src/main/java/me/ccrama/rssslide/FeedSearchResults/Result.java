
package me.ccrama.rssslide.FeedSearchResults;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliciousTags",
    "feedId",
    "title",
    "language",
    "lastUpdated",
    "subscribers",
    "velocity",
    "score",
    "website",
    "coverage",
    "coverageScore",
    "estimatedEngagement",
    "contentType",
    "scheme",
    "description",
    "coverUrl",
    "iconUrl",
    "partial",
    "visualUrl",
    "coverColor",
    "art",
    "hint"
})
public class Result {

    @JsonProperty("deliciousTags")
    private List<String> deliciousTags = null;
    @JsonProperty("feedId")
    private String feedId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("language")
    private String language;
    @JsonProperty("lastUpdated")
    private Integer lastUpdated;
    @JsonProperty("subscribers")
    private Integer subscribers;
    @JsonProperty("velocity")
    private Double velocity;
    @JsonProperty("score")
    private Integer score;
    @JsonProperty("website")
    private String website;
    @JsonProperty("coverage")
    private Double coverage;
    @JsonProperty("coverageScore")
    private Double coverageScore;
    @JsonProperty("estimatedEngagement")
    private Integer estimatedEngagement;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("scheme")
    private String scheme;
    @JsonProperty("description")
    private String description;
    @JsonProperty("coverUrl")
    private String coverUrl;
    @JsonProperty("iconUrl")
    private String iconUrl;
    @JsonProperty("partial")
    private Boolean partial;
    @JsonProperty("visualUrl")
    private String visualUrl;
    @JsonProperty("coverColor")
    private String coverColor;
    @JsonProperty("art")
    private Double art;
    @JsonProperty("hint")
    private String hint;

    @JsonProperty("deliciousTags")
    public List<String> getDeliciousTags() {
        return deliciousTags;
    }

    @JsonProperty("deliciousTags")
    public void setDeliciousTags(List<String> deliciousTags) {
        this.deliciousTags = deliciousTags;
    }

    @JsonProperty("feedId")
    public String getFeedId() {
        return feedId;
    }

    @JsonProperty("feedId")
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    @JsonProperty("lastUpdated")
    public Integer getLastUpdated() {
        return lastUpdated;
    }

    @JsonProperty("lastUpdated")
    public void setLastUpdated(Integer lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @JsonProperty("subscribers")
    public Integer getSubscribers() {
        return subscribers;
    }

    @JsonProperty("subscribers")
    public void setSubscribers(Integer subscribers) {
        this.subscribers = subscribers;
    }

    @JsonProperty("velocity")
    public Double getVelocity() {
        return velocity;
    }

    @JsonProperty("velocity")
    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    @JsonProperty("score")
    public Integer getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(Integer score) {
        this.score = score;
    }

    @JsonProperty("website")
    public String getWebsite() {
        return website;
    }

    @JsonProperty("website")
    public void setWebsite(String website) {
        this.website = website;
    }

    @JsonProperty("coverage")
    public Double getCoverage() {
        return coverage;
    }

    @JsonProperty("coverage")
    public void setCoverage(Double coverage) {
        this.coverage = coverage;
    }

    @JsonProperty("coverageScore")
    public Double getCoverageScore() {
        return coverageScore;
    }

    @JsonProperty("coverageScore")
    public void setCoverageScore(Double coverageScore) {
        this.coverageScore = coverageScore;
    }

    @JsonProperty("estimatedEngagement")
    public Integer getEstimatedEngagement() {
        return estimatedEngagement;
    }

    @JsonProperty("estimatedEngagement")
    public void setEstimatedEngagement(Integer estimatedEngagement) {
        this.estimatedEngagement = estimatedEngagement;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonProperty("scheme")
    public String getScheme() {
        return scheme;
    }

    @JsonProperty("scheme")
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("coverUrl")
    public String getCoverUrl() {
        return coverUrl;
    }

    @JsonProperty("coverUrl")
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @JsonProperty("iconUrl")
    public String getIconUrl() {
        return iconUrl;
    }

    @JsonProperty("iconUrl")
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @JsonProperty("partial")
    public Boolean getPartial() {
        return partial;
    }

    @JsonProperty("partial")
    public void setPartial(Boolean partial) {
        this.partial = partial;
    }

    @JsonProperty("visualUrl")
    public String getVisualUrl() {
        return visualUrl;
    }

    @JsonProperty("visualUrl")
    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }

    @JsonProperty("coverColor")
    public String getCoverColor() {
        return coverColor;
    }

    @JsonProperty("coverColor")
    public void setCoverColor(String coverColor) {
        this.coverColor = coverColor;
    }

    @JsonProperty("art")
    public Double getArt() {
        return art;
    }

    @JsonProperty("art")
    public void setArt(Double art) {
        this.art = art;
    }

    @JsonProperty("hint")
    public String getHint() {
        return hint;
    }

    @JsonProperty("hint")
    public void setHint(String hint) {
        this.hint = hint;
    }

}
