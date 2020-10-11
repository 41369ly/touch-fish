package cn.tybblog.touchfish;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.util.HttpRequest;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class editorKeyListener implements EditorFactoryListener {
    private PersistentState persistentState = PersistentState.getInstance();
    private Integer bookIndex;
    private Book book;
    private Integer chapterIndex;
    private Chapter chapter;
    private Integer maxRow = 0;
    private String[] bookText;
    private boolean flag = false;

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();
        Project project = editor.getProject();
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        EditorImpl editor1 = (EditorImpl) editor;
        editor1.getContentComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                String modifiersText = e.getKeyModifiersText(e.getModifiers());
                String key = "";
                if (modifiersText != null && !"".equals(modifiersText))
                    key += modifiersText + "+";
                if (key.indexOf(e.getKeyText(e.getKeyCode())) == -1)
                    key += e.getKeyText(e.getKeyCode());
                else
                    key = key.substring(0, key.length() - 1);
                if (key.indexOf("箭头") > -1)
                    key = key.replaceAll("向上箭头", "↑")
                            .replaceAll("向下箭头", "↓")
                            .replaceAll("向右箭头", "→")
                            .replaceAll("向左箭头", "←");
                String[] stateKey = persistentState.getKey();
                if(stateKey==null) return;
                if (stateKey[4].equals(key))
                    flag=false;
                if (flag) {
                    super.keyPressed(e);
                    return;
                }
                for (int i = 0; i < stateKey.length; i++) {
                    if (i==4) continue;
                    if (stateKey[i].equals(key)) {
                        if (bookIndex != persistentState.getBookIndex()) {
                            bookIndex = persistentState.getBookIndex();
                            List<Book> books = persistentState.getBook();
                            if(books==null||books.size()==0) {
                                statusBar.setInfo("快去书架添加书吧！");
                                return;
                            }
                            book = books.get(bookIndex);
                            chapterIndex = book.getIndex();
                            if (book.getChapters()==null){
                                try {
                                    book.setChapters(loadChapters());
                                }catch (Exception e1){
                                    statusBar.setInfo("网络连接失败...");
                                    return;
                                }
                                persistentState.setBook(books);
                            }

                            chapter = book.getChapters().get(chapterIndex);
                        }
                        switch (i) {
                            case 0:
                                if (chapter.getRow()<=0) {
                                    preChapters(statusBar);
                                    break;
                                }
                                if (bookText==null) loadBook(statusBar);
                                chapter.setRow(chapter.getRow() - 1);
                                if (bookText.length-1>=chapter.getRow())
                                    statusBar.setInfo(bookText[chapter.getRow()]);
                                break;
                            case 1:
                                if (bookText==null) loadBook(statusBar);
                                if (chapter.getRow()>=maxRow) {
                                    if (nextChapters(statusBar)) break;
                                }
                                if (bookText.length-1>=chapter.getRow())
                                    statusBar.setInfo(bookText[chapter.getRow()]);
                                chapter.setRow(chapter.getRow() + 1);
                                break;
                            case 2:
                                preChapters(statusBar);
                                if (book!=null&&book.getChapters()!=null)
                                    statusBar.setInfo("当前章节："+book.getChapters().get(book.getIndex()).getTitle());
                                break;
                            case 3:
                                nextChapters(statusBar);
                                statusBar.setInfo("当前章节："+book.getChapters().get(book.getIndex()).getTitle());
                                break;
                            case 5:
                                statusBar.setInfo("");
                                flag=true;
                                break;
                        }
                        break;
                    }
                }
            }
        });
    }

    private List<Chapter> loadChapters() throws IOException {
        String bookHtml = HttpRequest.sendZipGet(book.getUrl());
        Document document = Jsoup.parse(bookHtml);
        Elements elements = document.select("#list dl dd a");
        List<Chapter> chapters=new ArrayList<>();
        for (Element element : elements) {
            Chapter chapter=new Chapter();
            chapter.setTitle(element.text());
            chapter.setRow(0);
            chapter.setUrl(element.attr("href"));
            chapters.add(chapter);
        }
        return chapters;
    }

    private void loadBook(StatusBar statusBar){
        String bookHtml = "";
        try {
            bookHtml=HttpRequest.sendZipGet("http://www.xbiquge.la" + chapter.getUrl());
        }catch (Exception e){
            statusBar.setInfo("网络连接失败...");
            return;
        }
        Document document = Jsoup.parse(bookHtml);
        document.select("#content p").remove();
        String html = document.select("#content").html();
        String[] bookText = html.replaceAll("&nbsp;", "").replaceAll(" \n","").split("<br><br>");
        maxRow=bookText.length;
        this.bookText=bookText;
    }

    private boolean nextChapters(StatusBar statusBar){
        if (book.getIndex() >= book.getChapters().size()) {
            try {
                loadChapters();
            } catch (Exception e) {
                statusBar.setInfo("网络连接失败...");
            }
            if (book.getIndex() > book.getChapters().size()) {
                statusBar.setInfo("到底了！");
                return true;
            }
        }
        book.setIndex(book.getIndex() + 1);
        chapter=book.getChapters().get(book.getIndex());
        loadBook(statusBar);
        return false;
    }

    private void preChapters(StatusBar statusBar){
        if (book==null||book.getIndex()<=0) {
            statusBar.setInfo("到顶了！");
            return;
        }
        book.setIndex(book.getIndex() - 1);
        chapter=book.getChapters().get(book.getIndex());
        loadBook(statusBar);
    }
}
