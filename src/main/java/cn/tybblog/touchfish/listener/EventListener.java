package cn.tybblog.touchfish.listener;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import cn.tybblog.touchfish.exception.FishException;
import cn.tybblog.touchfish.util.ChapterCallback;
import cn.tybblog.touchfish.util.ConsoleUtils;
import cn.tybblog.touchfish.util.KeyMapFormatUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class EventListener implements KeyEventPostProcessor, AWTEventListener, ChapterCallback {
    private PersistentState persistentState = PersistentState.getInstance();
    private StatusBar statusBar;
    public static Book book;
    private boolean flag = false;
    private List<String> cacheRow;
    private int cacheIndex = -1;
    private List<String> bookText;
    public static boolean loading=false;

    public static String LOADING_TEXT = "������...";

    public EventListener(Project project){
        statusBar = WindowManager.getInstance().getStatusBar(project);
    }

    @Override
    public boolean postProcessKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }
        String key = KeyMapFormatUtils.keyMapFormat(e);
        if ("".equals(key)) {
            return false;
        }
        try {
            doRead(key);
        } catch (FishException fishException) {
            ConsoleUtils.info(fishException.getMessage());
            if(!LOADING_TEXT.equals(fishException.getMessage())) {
                EventListener.loading = false;
            }
        }
        return false;
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) event;
            if (e.getButton()<4) {
                return;
            }
            try {
                doRead("�����"+(e.getButton()-3));
            } catch (FishException fishException) {
                ConsoleUtils.info(fishException.getMessage());
                EventListener.loading=false;
            }
        }
    }

    public void doRead(String key) throws FishException {
        String[] stateKey = persistentState.getKey();
        if(stateKey==null) {
            return;
        }
        if (loading){
            return;
        }
        if (book==null){
            initBook();
            return;
        }
        if (stateKey[4].equals(key)) {
            flag=!flag;
            if (flag) {
                ConsoleUtils.info("");
            }
            return;
        }
        if (flag) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (!stateKey[i].equals(key)) {
                continue;
            }
            switch (i) {
                case 0:
                    preCacheRow();
                    break;
                case 1:
                    nextCacheRow();
                    break;
                case 2:
//                    preChapter();
                    break;
                case 3:
//                    nextChapter();
                    break;
                default:
            }
            break;
        }
    }

    public void initBook() throws FishException {
        book=persistentState.getBookByIndex();
        bookText=book.loadChapter(this,Book.BASE_METHOD_NEXT);
        Chapter chapter = persistentState.getBookByIndex().getChapterByIndex();
        if (chapter.nextRow()>=bookText.size()){
            chapter.setRow(bookText.size()-1);
        } else {
            splitBookText(chapter.getRow());
        }
        cacheIndex=-1;
        nextCacheRow();
    }

    /**
     * ��һ��
     * @return �Ƿ���سɹ�
     */
    private void preInfo() throws FishException {
        Chapter chapter = persistentState.getBookByIndex().getChapterByIndex();
        if (chapter.preRow()<0){
            bookText=book.preChapter(this);
            chapter = persistentState.getBookByIndex().getChapterByIndex();
            chapter.setRow(bookText.size()-1);
        }
        splitBookText(chapter.getRow());
        cacheIndex=cacheRow.size();
    }

    /**
     * ��һ��
     * @return �Ƿ���سɹ�
     */
    private void nextInfo() throws FishException {
        Chapter chapter = persistentState.getBookByIndex().getChapterByIndex();
        if (chapter.nextRow()>=bookText.size()){
            bookText=book.nextChapter(this);
            chapter = persistentState.getBookByIndex().getChapterByIndex();
            if (chapter.nextRow()>=bookText.size()){
                chapter.setRow(bookText.size()-1);
            }
        }
        splitBookText(chapter.getRow());
        cacheIndex=-1;
    }

    /**
     * ��һ�л���
     */
    private void preCacheRow() throws FishException {
        if (cacheRow == null || cacheRow.size() == 0 || cacheIndex < 1) {
            preInfo();
        }
        --cacheIndex;
        showCacheRow();
    }

    /**
     * ��һ�л���
     *
     * @return �Ƿ��л���
     */
    private void nextCacheRow() throws FishException {
        if (cacheRow == null || cacheRow.size() == 0 || cacheIndex+1 >= cacheRow.size()) {
            nextInfo();
        }
        ++cacheIndex;
        showCacheRow();
    }

    /**
     * ��ʾ������
     */
    private void showCacheRow(){
        if (cacheRow == null || cacheRow.size() == 0 || cacheIndex < 0 || cacheIndex >= cacheRow.size()) {
            ConsoleUtils.info("���ػ�����ʱ����δ֪����");
            return;
        }
        ConsoleUtils.info(cacheRow.get(cacheIndex));
    }

    /**
     * ���ݿ���̨���ȷָ��ַ���
     */
    private void splitBookText(int row){
        cacheRow = Lists.newArrayList(Splitter.fixedLength(getConsoleLen()).split(bookText.get(row)));
    }

    /**
     * ��ȡ����̨����ʾ����
     * @return ����̨����ʾ����
     */
    private int getConsoleLen(){
        int textLen = 0;
        if (statusBar instanceof IdeStatusBarImpl) {
            IdeStatusBarImpl bar = (IdeStatusBarImpl) statusBar;
            int width = bar.getWidth();
            textLen = width / 17;
        }
        if(textLen==0){
            MessageDialogBuilder.yesNo("��ʾ", "�Զ���ȡ����̨����ʱ����").show();
            textLen=10;
        }
        return textLen;
    }

    /**
     * �첽�ص�
     * @param bookText �鱾����
     * @param baseMethod ��Դ����
     */
    @Override
    public void chapter(List<String> bookText, String baseMethod) throws FishException {
        loading=false;
        this.bookText=bookText;
        Chapter chapter = persistentState.getBookByIndex().getChapterByIndex();
        if (Book.BASE_METHOD_NEXT.equals(baseMethod)){
            if (chapter.nextRow()>=bookText.size()){
                chapter.setRow(bookText.size()-1);
            } else {
                splitBookText(chapter.getRow());
            }
            cacheIndex=-1;
            nextCacheRow();
        } else if (Book.BASE_METHOD_PRE.equals(baseMethod)){
            chapter.setRow(bookText.size());
            splitBookText(chapter.preRow());
            cacheIndex=cacheRow.size();
            preCacheRow();
        }
    }

}
