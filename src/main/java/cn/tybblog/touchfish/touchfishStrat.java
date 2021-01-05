package cn.tybblog.touchfish;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.util.ReadBook;
import cn.tybblog.touchfish.util.impl.ReadBookImpl;
import cn.tybblog.touchfish.util.impl.ReadFileBookImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class touchfishStrat implements StartupActivity.DumbAware {

    private PersistentState persistentState = PersistentState.getInstance();
    private Integer bookIndex;
    private ReadBook bookUtil;
    private boolean flag = false;

    @Override
    public void runActivity(@NotNull Project project) {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventPostProcessor(new KeyEventPostProcessor() {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED) {
                    return false;
                }
                String modifiersText = e.getKeyModifiersText(e.getModifiers());
                String key = "";
                if (modifiersText != null && !"".equals(modifiersText))
                    key += modifiersText + "+";
                if (key.indexOf(e.getKeyText(e.getKeyCode())) == -1)
                    key += e.getKeyText(e.getKeyCode());
                else
                    key = key.substring(0, key.length() - 1);
                if (key.indexOf("��ͷ") > -1)
                    key = key.replaceAll("���ϼ�ͷ", "��")
                            .replaceAll("���¼�ͷ", "��")
                            .replaceAll("���Ҽ�ͷ", "��")
                            .replaceAll("�����ͷ", "��");
                String[] stateKey = persistentState.getKey();
                if(stateKey==null) return false;
                if (stateKey[4].equals(key))
                    flag=false;
                if (flag) {
                    return true;
                }
                for (int i = 0; i < stateKey.length; i++) {
                    if (i==4) continue;
                    if (!stateKey[i].equals(key)) continue;
                    if (bookUtil==null||bookIndex==null||bookIndex!=persistentState.getBookIndex()){
                        bookIndex=persistentState.getBookIndex();
                        List<Book> books = persistentState.getBook();
                        if (books!=null&&books.size()>0){
                            Book book = books.get(bookIndex);
                            //�ж��Ƿ�Ϊ�ļ��鼮
                            if (book.getAuth().equals("�Զ��嵼��")){
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
                return false;
            }
        });
    }
}
