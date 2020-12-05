package cn.tybblog.touchfish.util.impl;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.util.ReadBook;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadFileBookImpl implements ReadBook {

    private PersistentState persistentState = PersistentState.getInstance();
    private Integer bookIndex;
    private Book book;
    private Chapter chapter;
    private StatusBar statusBar;
    private List<String> bookText;
    private String cacheRow;

    public ReadFileBookImpl(StatusBar statusBar){
        this.statusBar=statusBar;
    }

    @Override
    public void preChapter() {
        statusBar.setInfo("文件格式不支持上一章操作");
    }

    @Override
    public void nextChapter() {
        statusBar.setInfo("文件格式不支持下一章操作");
    }

    @Override
    public void info() {
        persistentState.getBook().get(persistentState.getBookIndex());
        bookIndex = persistentState.getBookIndex();
        List<Book> books = persistentState.getBook();
        if (books == null || books.size() == 0) {
            statusBar.setInfo("快去书架添加书吧！");
            return;
        }
        book = books.get(bookIndex);
        getChapters();
    }

    private void getChapters(){
        chapter = book.getChapters().get(book.getIndex());
        LineIterator file = null;
        try {
            file = FileUtils.lineIterator(new File(chapter.getUrl()), "UTF-8");
            bookText = new ArrayList<>();
            while (file.hasNext()) {
                bookText.add(file.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void nextInfo() {
        if(chapter==null||book==null) info();
        if (cacheRow==null && chapter.getRow()+1 >= bookText.size()){
            if (book.getIndex() + 1 >= book.getChapters().size()) {
                statusBar.setInfo("到底了！");
                return;
            }
            book.setIndex(book.getIndex() + 1);
            getChapters();
        }
        if (cacheRow==null)
            chapter.setRow(chapter.getRow()+1);
        setInfo();
    }

    @Override
    public void preInfo() {
        if(chapter==null) info();
        if (chapter.getRow()>-1)
            chapter.setRow(chapter.getRow()-1);
        if (chapter.getRow() < 0){
            if (book.getIndex() <= 0) {
                statusBar.setInfo("到底了！");
                return;
            }
            book.setIndex(book.getIndex() - 1);
            getChapters();
        }
        cacheRow=null;
        setInfo();
    }
    public void setInfo() {
        String rowText = cacheRow == null ? bookText.get(chapter.getRow()) : cacheRow;
        int textlen = 0;
        if (statusBar instanceof IdeStatusBarImpl) {
            IdeStatusBarImpl bar = (IdeStatusBarImpl) statusBar;
            int width = bar.getWidth();
            textlen = width / 17;
        }
        if (textlen != 0 && rowText.length() > textlen) {
            cacheRow = rowText.substring(textlen);
            rowText = rowText.substring(0, rowText.length() - cacheRow.length());
        } else {
            cacheRow = null;
        }
        statusBar.setInfo(rowText);
    }

}
