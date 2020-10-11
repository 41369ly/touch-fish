package cn.tybblog.touchfish.entity;

import java.util.List;

public class Book {
    private String url;
    private String bookName;
    private String auth;
    private Integer index;
    private List<Chapter> chapters;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public Book(String url, String bookName, String auth) {
        this.url = url;
        this.bookName = bookName;
        this.auth = auth;
        this.index = 0;
    }

    public Book() {
    }
}
