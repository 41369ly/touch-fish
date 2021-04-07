package cn.tybblog.touchfish.entity;

public class Chapter {
    private String url;
    private String title;
    private Integer row;

    public Chapter() {
    }

    public Chapter(String url, String title) {
        this.url = url;
        this.title = title;
        this.row = -1;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public int nextRow(){return ++row;}

    public int preRow(){
        if (row<0){
            return row;
        }
        return --row;
    }

    @Override
    public String toString(){
        return title.trim();
    }
}
