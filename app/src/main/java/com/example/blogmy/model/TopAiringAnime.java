package com.example.blogmy.model;


import java.util.List;

public class TopAiringAnime{
    private String requestHash;
    private Boolean requestCached;
    private Integer requestCacheExpiry;
    private List<Top> top = null;

    /**
     * No args constructor for use in serialization
     *
     */
    public TopAiringAnime() {
    }

    /**
     *
     * @param requestCached
     * @param requestHash
     * @param requestCacheExpiry
     * @param top
     */
    public TopAiringAnime(String requestHash, Boolean requestCached, Integer requestCacheExpiry, List<Top> top) {
        super();
        this.requestHash = requestHash;
        this.requestCached = requestCached;
        this.requestCacheExpiry = requestCacheExpiry;
        this.top = top;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public Boolean getRequestCached() {
        return requestCached;
    }

    public void setRequestCached(Boolean requestCached) {
        this.requestCached = requestCached;
    }

    public Integer getRequestCacheExpiry() {
        return requestCacheExpiry;
    }

    public void setRequestCacheExpiry(Integer requestCacheExpiry) {
        this.requestCacheExpiry = requestCacheExpiry;
    }

    public List<Top> getTop() {
        return top;
    }

    public void setTop(List<Top> top) {
        this.top = top;
    }



}
