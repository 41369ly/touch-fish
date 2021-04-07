package cn.tybblog.touchfish.ui.field;

import cn.tybblog.touchfish.util.NetworkUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;

import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Vector;

public class SearchTextField extends JPanel {

    private JTextField myTextField;
    private JBPopup myPopup;
    private JList<String> list;
    private JButton btn;
    public SearchTextField() {
        super(new BorderLayout());
        myTextField = new JTextField();
        list = new JList<>();
        myTextField.setForeground(Color.GRAY);
        myTextField.setText("请输入书名");
        myTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search(myTextField.getText());

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search(myTextField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        myTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int keycode = e.getKeyCode();
                if (keycode == KeyEvent.VK_DOWN) {
                    int index = list.getSelectedIndex();
                    if (index < list.getLastVisibleIndex()) {
                        list.setSelectedIndex(++index);
                    }
                } else if (keycode == KeyEvent.VK_UP) {
                    int index = list.getSelectedIndex();
                    if (index > list.getFirstVisibleIndex()) {
                        list.setSelectedIndex(--index);
                    }
                }else if(keycode == KeyEvent.VK_ENTER){
                    String value = list.getSelectedValue();
                    if (value == null) {
                        if (btn!=null) {
                            btn.doClick();
                        }
                        return;
                    }
                    myTextField.setText(value);
                    if (myPopup != null) {
                        myPopup.cancel();
                        myPopup = null;
                    }
                }
            }
        });
        myTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //获取焦点时，清空提示内容
                String temp = myTextField.getText();
                if (temp.equals("请输入书名")) {
                    myTextField.setText("");
                    myTextField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                //失去焦点时，没有输入内容，显示提示内容
                String temp = myTextField.getText();
                if (temp.equals("")) {
                    myTextField.setForeground(Color.GRAY);
                    myTextField.setText("请输入书名");
                }
            }
        });
        myTextField.setColumns(15);
        add(myTextField);
    }

    protected Runnable createItemChosenCallback(final JList list) {
        return () -> {
            final String value = (String) list.getSelectedValue();
            myTextField.setText(value != null ? value : "");
            if (myPopup != null) {
                myPopup.cancel();
                myPopup = null;
            }
        };
    }

    public void callBack(JButton btn){
        this.btn=btn;
    }

    protected Component getPopupLocationComponent() {
        return this;
    }

    private void search(String bookName) {
        if ("".equals(bookName)) {
            return;
        }
        String jsonp = "";
        try {
            jsonp = NetworkUtil.sendRequest("http://unionsug.baidu.com/su?wd=" + URLEncoder.encode(bookName, "utf-8") + "&cb=a&t=" + System.currentTimeMillis());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int startIndex = jsonp.indexOf("(");
        int endIndex = jsonp.lastIndexOf(")");
        String jsonstr = jsonp.substring(startIndex+1, endIndex);
        JSONObject json=null;
        try {
            json = JSON.parseObject(jsonstr);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        JSONArray s = json.getJSONArray("s");
        if (s.size() == 0) {
            return;
        }
        Vector<String> strings = new Vector<String>();
        for (Object o : s) {
            strings.add(o + "");
        }
        if (list == null) {
            list = new JList<String>();
        }
        list.setListData(strings);
        if (myPopup == null || !myPopup.isVisible()) {
            myPopup = JBPopupFactory.getInstance().createListPopupBuilder(list)
                    .setMovable(false)
                    .setRequestFocus(false)
                    .setItemChoosenCallback(createItemChosenCallback(list)).createPopup();
            myPopup.showUnderneathOf(getPopupLocationComponent());
        }
        Dimension size = myPopup.getSize();
        size.height = 20 * strings.size();
        size.width = myTextField.getWidth()-6;
        myPopup.setSize(size);
    }

    public String getText() {
        return myTextField.getText();
    }
}
