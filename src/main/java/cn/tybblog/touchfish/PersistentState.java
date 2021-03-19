package cn.tybblog.touchfish;

import cn.tybblog.touchfish.entity.Book;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


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

    public boolean isConsole() {
        return isConsole;
    }

    public void setConsole(boolean console) {
        isConsole = console;
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

}