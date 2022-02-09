package cn.tybblog.touchfish;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.exception.FishException;
import cn.tybblog.touchfish.util.FileSplitUtils;
import cn.tybblog.touchfish.util.NetworkUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author ly
 */
@State(
        name = "PersistentState",
        storages = {@Storage(
                value = "touch-fish.xml"
        )}
)
public class PersistentState implements PersistentStateComponent<PersistentState> {

    /** 书架 */
    private List<Book> book;
    /** 热键 */
    private String[] key;
    /** 书架索引  */
    private Integer bookIndex;
    /** V1.5 新增 数据源 */
    private String url;
    /** 是否使用控制台 */
    private boolean isConsole;
    /** 自动翻页 */
    private Integer nextInfoTime;
    /** 字体大小 */
    private String fontStyle;
    /** 章节的正则匹配 */
    private String regexpStr;

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public Integer getNextInfoTime() {
        return nextInfoTime;
    }

    public void setNextInfoTime(Integer nextInfoTime) {
        this.nextInfoTime = nextInfoTime;
    }

    public boolean getIsConsole() {
        return isConsole;
    }

    public void setIsConsole(boolean console) {
        this.isConsole = console;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Book> getBook() {
        return book;
    }

    public void setBook(List<Book> book) {
        this.book = book;
    }

    public String[] getKey() {
        return key;
    }

    public void setKey(String[] key) {
        this.key = key;
    }

    public Integer getBookIndex() {
        return bookIndex==null||bookIndex<0?0:bookIndex;
    }

    public void setBookIndex(Integer bookIndex) {
        this.bookIndex = bookIndex;
    }

    /**
     * 新增书
     * @param book
     * @return 是否新增成功
     */
    public boolean addBook(Book book) {
        if (this.book ==null) {
            this.book=new ArrayList<>();
        }
        for (Book book1 : this.book) {
            if (book1.getUrl().equals(book.getUrl())) {
                return false;
            }
        }
        //自定义导入书籍分割文件
        if (Book.FILE_AUTH.equals(book.getAuth())) {
            Book split = FileSplitUtils.split(book);
            if (split==null) {
                MessageDialogBuilder.yesNo("提示","分割文件失败").show();
                return false;
            }
            book=split;
        }else{
            book.setChapters(NetworkUtil.getChapter(book.getUrl()));
        }
        this.book.add(book);
        return true;
    }

    /**
     * 删除书籍
     * @param index 索引
     * @return 是否删除成功
     */
    public boolean delBook(int index){
        if (index<0||index>book.size()) {
            return false;
        }
        if (Book.FILE_AUTH.equals(book.get(index).getAuth())) {
            for (Chapter chapter : book.get(index).getChapters()) {
                new File(chapter.getUrl()).delete();
            }
        }
        book.remove(index);
        if (index==bookIndex) {
            bookIndex--;
        }
        return true;
    }

    /**
     * 更新热键
     * @param key
     * @param index
     */
    public void setKeyMap(String key,int index){
        this.key[index]=key;
    }


    /**
     * 获取当前选中书籍
     * @return 当前选中书籍
     */
    public Book getBookByIndex() throws FishException {
        if(book==null||book.size()==0){
            FishException.throwFishException("书架中还没有书,赶紧去添加吧！");
        }
        if (bookIndex<0 || bookIndex>=book.size()){
            this.bookIndex=book.size()-1;
        }
        return this.book.get(bookIndex);
    }

    public static PersistentState getInstance() {
        return ServiceManager.getService(PersistentState.class);
    }

    @Nullable
    @Override
    public PersistentState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PersistentState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getRegexpStr() {
        return regexpStr;
    }

    public void setRegexpStr(String regexpStr) {
        this.regexpStr = regexpStr;
    }
}