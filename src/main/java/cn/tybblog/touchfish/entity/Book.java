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
    public static String FILE_AUTH="�Զ��嵼��";
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
     * ��ȡ��ǰ�½�
     * @return
     */
    public Chapter getChapterByIndex() throws FishException {
        if (chapters==null||chapters.size()==0){
            FishException.throwFishException("�½�Ϊ��");
        }
        if(index>=chapters.size()){
            index=chapters.size()-1;
        }
        return chapters.get(index<0?0:index);
    }

    /**
     * �����½�
     * @param callback �첽�ص���������
     * @return ��������
     */
    public List<String> loadChapter(ChapterCallback callback,String baseMethod) throws FishException {
        List<String> bookText = new ArrayList<>();
        Chapter chapter = getChapterByIndex();
        if (chapter==null){
            FishException.throwFishException("�½�Ϊ�գ����������");
        }
        if (!auth.equals(FILE_AUTH)&&index>=chapters.size()-1){
            //�����鼮����ȡ�����½ڣ��������ڸ���
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
            FishException.throwFishException("������...");
        }
        return bookText;
    }

    /**
     * ��һ��
     * @param callback �첽�ص���������
     * @return ��������
     */
    public List<String> nextChapter(ChapterCallback callback) throws FishException {
        if(index>=chapters.size()){
            return loadChapter(callback,BASE_METHOD_NEXT);
        }
        index++;
        return loadChapter(callback,BASE_METHOD_NEXT);
    }

    /**
     * ��һ��
     * @param callback �첽�ص���������
     * @return ��������
     */
    public List<String> preChapter(ChapterCallback callback) throws FishException {
        if(index<0){
            FishException.throwFishException("������Ŷ");
        }
        index--;
        return loadChapter(callback,BASE_METHOD_PRE);
    }

    public String nextIndex() throws FishException {
        if (index<chapters.size()) {
            index++;
        } else {
            return "�Ѿ������һ����";
        }
        getChapterByIndex().setRow(-1);
        return getChapterByIndex().getTitle();
    }

    public String preIndex() throws FishException {
        if(index<0){
            return "�Ѿ��ǵ�һ����";
        }
        index--;
        getChapterByIndex().setRow(-1);
        return getChapterByIndex().getTitle();
    }
}
