package cn.tybblog.touchfish.listener;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.util.ReadBook;
import cn.tybblog.touchfish.util.impl.ReadBookImpl;
import cn.tybblog.touchfish.util.impl.ReadFileBookImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class EventListener implements KeyEventPostProcessor,AWTEventListener {
    private StatusBar statusBar;
    private PersistentState persistentState = PersistentState.getInstance();
    private Integer bookIndex;
    private ReadBook bookUtil;
    private boolean flag = false;

    public EventListener(Project project){
        statusBar=WindowManager.getInstance().getStatusBar(project);
    }

    @Override
    public boolean postProcessKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }
        String modifiersText = e.getKeyModifiersText(e.getModifiers());
        String key = "";
        if (modifiersText != null && !"".equals(modifiersText)) {
            key += modifiersText + "+";
        }
        if (key.indexOf(KeyEvent.getKeyText(e.getKeyCode())) == -1) {
            key += KeyEvent.getKeyText(e.getKeyCode());
        } else {
            key = key.substring(0, key.length() - 1);
        }
        if (key.indexOf("箭头") > -1) {
            key = key.replaceAll("向上箭头", "↑")
                    .replaceAll("向下箭头", "↓")
                    .replaceAll("向右箭头", "→")
                    .replaceAll("向左箭头", "←");
        }
        doread(key);
        return false;
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) event;
            if (e.getButton()<4) {
                return;
            }
            doread("鼠标侧键"+(e.getButton()-3));
        }
    }

    public void doread(String key){
        String[] stateKey = persistentState.getKey();
        if(stateKey==null) {
            return;
        }
        if (stateKey[4].equals(key)) {
            flag=false;
        }
        if (flag) {
            return;
        }
        for (int i = 0; i < stateKey.length; i++) {
            if (i==4) {
                continue;
            }
            if (!stateKey[i].equals(key)) {
                continue;
            }
            if (bookUtil==null||bookIndex==null|| !bookIndex.equals(persistentState.getBookIndex())){
                bookIndex=persistentState.getBookIndex();
                List<Book> books = persistentState.getBook();
                if (books!=null&&books.size()>0){
                    Book book = books.get(bookIndex);
                    //判断是否为文件书籍
                    if (book.getAuth().equals("自定义导入")){
                        bookUtil=new ReadFileBookImpl(statusBar);
                    } else {
                        bookUtil=new ReadBookImpl(statusBar);
                    }
                }
                bookUtil.info();
            }
            switch (i) {
                case 0:
                    bookUtil.preInfo();
                    break;
                case 1:
                    bookUtil.nextInfo();
                    break;
                case 2:
                    bookUtil.preChapter();
                    break;
                case 3:
                    bookUtil.nextChapter();
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
