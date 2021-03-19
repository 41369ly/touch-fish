package cn.tybblog.touchfish.util;

import com.intellij.openapi.ui.MessageDialogBuilder;
import okhttp3.Call;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public abstract class HttpCallBack {
    public void onFailure(Call call, IOException e) {
        if (e instanceof SocketTimeoutException) {
            MessageDialogBuilder.yesNo("��ʾ", "���س�ʱ��").show();
        }
        if (e instanceof ConnectException||e instanceof UnknownHostException) {
            MessageDialogBuilder.yesNo("��ʾ", "��������ʧ�ܣ�").show();
        }
    }

    public abstract <T> void success(T data);
}
