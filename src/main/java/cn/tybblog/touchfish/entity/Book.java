package cn.tybblog.touchfish.entity;

import cn.tybblog.touchfish.exception.FishException;
import cn.tybblog.touchfish.util.ChapterCallback;
import cn.tybblog.touchfish.util.FileCode;
import cn.tybblog.touchfish.util.NetworkUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private String url;
    private String bookName;
    public static String FILE_AUTH="自定义导入";
    private String auth;
    private Integer index;
    private List<Chapter> chapters;

    public static String BASE_METHOD_PRE="pre";
    public static String BASE_METHOD_NEXT="next";

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
        this.index = -1;
        this.chapters = new ArrayList<>();
    }

    public Book() {
    }

    /**
     * 获取当前章节
     * @return
     */
    public Chapter getChapterByIndex() throws FishException {
        if (chapters==null||chapters.size()==0){
            FishException.throwFishException("章节为空");
        }
        if(index>=chapters.size()){
            index=chapters.size()-1;
        }
        return chapters.get(index<0?0:index);
    }

    /**
     * 加载章节
     * @param callback 异步回调文章内容
     * @return 文章内容
     */
    public List<String> loadChapter(ChapterCallback callback,String baseMethod) throws FishException {
        List<String> bookText = new ArrayList<>();
        Chapter chapter = getChapterByIndex();
        if (chapter==null){
            FishException.throwFishException("章节为空，请重新添加");
        }
        if (!auth.equals(FILE_AUTH)&&index>=chapters.size()-1){
            //网络书籍，获取最新章节，可能正在更新
            chapters=NetworkUtil.getChapter(url);
            chapter = getChapterByIndex();
        }
        if(auth.equals(FILE_AUTH)){
            try {
                bookText=FileUtils.readLines(new File(chapter.getUrl()), FileCode.codeString(chapter.getUrl()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            NetworkUtil.getBookText(chapter.getUrl(),callback,baseMethod);
            FishException.throwFishException("加载中...");
        }
        return bookText;
    }

    /**
     * 下一章
     * @param callback 异步回调文章内容
     * @return 文章内容
     */
    public List<String> nextChapter(ChapterCallback callback) throws FishException {
        if(index>=chapters.size()){
            return loadChapter(callback,BASE_METHOD_NEXT);
        }
        index++;
        return loadChapter(callback,BASE_METHOD_NEXT);
    }

    /**
     * 上一章
     * @param callback 异步回调文章内容
     * @return 文章内容
     */
    public List<String> preChapter(ChapterCallback callback) throws FishException {
        if(index<0){
            FishException.throwFishException("到顶了哦");
        }
        index--;
        return loadChapter(callback,BASE_METHOD_PRE);
    }

    public String nextIndex() throws FishException {
        if (index<chapters.size()) {
            index++;
        } else {
            return "已经是最后一章了";
        }
        getChapterByIndex().setRow(-1);
        return getChapterByIndex().getTitle();
    }

    public String preIndex() throws FishException {
        if(index<0){
            return "已经是第一章了";
        }
        index--;
        getChapterByIndex().setRow(-1);
        return getChapterByIndex().getTitle();
    }
}
