package com.example.blogmy.model;


// http://www.jsonschema2pojo.org/ use this to create class easily for model
public class Top{

    private Integer malId;
    private Integer rank;
    private String title;
    private String url;
    private String imageUrl;
    private String type;
    private Integer episodes;
    private String startDate;
    private String endDate;
    private Integer members;
    private Integer score;

    /**
     * No args constructor for use in serialization
     *
     */
    public Top() {
    }

    /**
     *
     * @param startDate
     * @param title
     * @param rank
     * @param imageUrl
     * @param score
     * @param endDate
     * @param type
     * @param malId
     * @param episodes
     * @param members
     * @param url
     */
    public Top(Integer malId, Integer rank, String title, String url, String imageUrl, String type, Integer episodes, String startDate, String endDate, Integer members, Integer score) {
        super();
        this.malId = malId;
        this.rank = rank;
        this.title = title;
        this.url = url;
        this.imageUrl = imageUrl;
        this.type = type;
        this.episodes = episodes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.members = members;
        this.score = score;
    }

    public Integer getMalId() {
        return malId;
    }

    public void setMalId(Integer malId) {
        this.malId = malId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Integer episodes) {
        this.episodes = episodes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getMembers() {
        return members;
    }

    public void setMembers(Integer members) {
        this.members = members;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

}
