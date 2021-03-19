package cn.tybblog.touchfish.util.impl;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.util.HttpRequest;
import cn.tybblog.touchfish.util.ReadBook;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ReadBookImpl implements ReadBook {

    private PersistentState persistentState = PersistentState.getInstance();

    private Integer bookIndex;
    private Book book;
    private Integer chapterIndex;
    private Chapter chapter;
    private Integer maxRow = 0;
    private String[] bookText;
    private String cacheRow;
    private StatusBar statusBar;
    private boolean flag= true;
    private String[] catcheBookText;
    private static ExecutorService executorService=Executors.newSingleThreadExecutor();


    public ReadBookImpl(StatusBar stausBar) {
        this.statusBar = stausBar;
    }

    @Override
    public void preChapter() {
        if (flag) return;
        catcheBookText=null;
        statusBar.setInfo("加载中...");
        flag = true;
        preChapters();
        if (book!=null&&book.getChapters()!=null)
            statusBar.setInfo("当前章节："+book.getChapters().get(book.getIndex()).getTitle());
        flag = false;
    }

    @Override
    public void nextChapter() {
        if (flag) return;
        catcheBookText=null;
        statusBar.setInfo("加载中...");
        flag = true;
        nextChapters();
        if (book!=null&&book.getChapters()!=null)
            statusBar.setInfo("当前章节："+book.getChapters().get(book.getIndex()).getTitle());
        flag = false;
    }

    @Override
    public void info() {
        bookIndex = persistentState.getBookIndex();
        List<Book> books = persistentState.getBook();
        if (books == null || books.size() == 0) {
            statusBar.setInfo("快去书架添加书吧！");
            return;
        }
        book = books.get(bookIndex);
        chapterIndex = book.getIndex();
        if (book.getChapters() == null) {
            book.setChapters(loadChapters());
            persistentState.setBook(books);
        }
        chapter = book.getChapters().get(chapterIndex);
        bookText = null;
        flag = false;
    }

    public List<Chapter> loadChapters() {
        String bookHtml = null;
        try {
            bookHtml = HttpRequest.sendZipGet(book.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document document = Jsoup.parse(bookHtml);
        Elements elements = document.select("#list dl dd a");
        List<Chapter> chapters = new ArrayList<>();
        for (Element element : elements) {
            Chapter chapter = new Chapter();
            chapter.setTitle(element.text());
            chapter.setRow(-1);
            chapter.setUrl(element.attr("href"));
            chapters.add(chapter);
        }
        return chapters;
    }

    public void loadBook() {
        if (catcheBookText == null) {
            statusBar.setInfo("加载中...");
            flag = true;
            executorService.execute(() -> {
                syncLoad(true);
            });
            executorService.execute(() -> {
                syncLoad(false);
            });
            return;
        }
        bookText=catcheBookText;
        catcheBookText=null;
        setInfo();
        executorService.execute(() -> {
            syncLoad(false);
        });
    }

    private void syncLoad(boolean flag){
            String bookHtml = "";
            try {
                bookHtml = HttpRequest.sendZipGet("http://www.paoshuzw.com" + chapter.getUrl());
            } catch (Exception e) {
                statusBar.setInfo("网络连接失败...");
                return;
            }
            Document document = Jsoup.parse(bookHtml);
            document.select("#content p").remove();
            String html = document.select("#content").html();
            String[] bookText = html.replaceAll("&nbsp;", "").replaceAll("\n<br>\n<br>", "").split(" \n<br> \n<br>");
            maxRow = bookText.length;
            if (flag) {
                this.bookText = bookText;
                setInfo();
                this.flag = false;
            } else
                this.catcheBookText = bookText;
    }

    public boolean nextChapters() {
        if (book.getChapters()==null) book.setChapters(loadChapters());
        if (book.getIndex() + 1 >= book.getChapters().size()) {
            loadChapters();
            if (book.getIndex() + 1 >= book.getChapters().size()) {
                statusBar.setInfo("到底了！");
                return true;
            }
        }
        book.setIndex(book.getIndex() + 1);
        chapter = book.getChapters().get(book.getIndex());
        return false;
    }

    public boolean preChapters() {
        if (book == null || book.getIndex() <= 0) {
            statusBar.setInfo("到顶了！");
            return true;
        }
        book.setIndex(book.getIndex() - 1);
        chapter = book.getChapters().get(book.getIndex());
        return false;
    }



    @Override
    public void nextInfo() {
        if (flag) return;
        if (bookText==null) {
            loadBook();
            return;
        }
        if (cacheRow==null && chapter.getRow()+1>=maxRow) {
            if (!nextChapters()) {
                chapter.setRow(chapter.getRow() + 1);
                loadBook();
            }
            return;
        }
        if (cacheRow==null)
            chapter.setRow(chapter.getRow() + 1);
        setInfo();
    }

    @Override
    public void preInfo() {
        if (flag) return;
        if (bookText==null) {
            loadBook();
            return;
        }
        if (chapter.getRow()>-1)
            chapter.setRow(chapter.getRow() - 1);
        cacheRow=null;
        if (chapter.getRow() < 0) {
            if (!preChapters()){
                loadBook();
            }
            return;
        }
        setInfo();
    }

    public void setInfo() {
        if(chapter.getRow()==-1) chapter.setRow(0);
        if (bookText.length-1<chapter.getRow()) return;
        String rowText = cacheRow == null ? bookText[chapter.getRow()] : cacheRow;
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
