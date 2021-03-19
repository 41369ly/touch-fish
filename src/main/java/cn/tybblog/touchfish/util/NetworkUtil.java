package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.PersistentState;
import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import com.intellij.openapi.ui.MessageDialogBuilder;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkUtil {
    private static OkHttpClient client = new OkHttpClient();
    private static PersistentState persistentState = PersistentState.getInstance();


    /**
     * ������
     * @param keyword �����ؼ���
     * @return html
     */
    public static List<Book> SearchBook(String keyword){
        //urlƴ��
        String url=persistentState.getUrl()+"/modules/article/waps.php?searchkey="+keyword;
        Document doc = Jsoup.parse(sendRequest(url));
        Elements trs = doc.select("#checkform table tr");
        List<Book> books = new ArrayList<Book>();
        if (trs == null||trs.size()==0) {
            return books;
        }
        //��һ��trΪ��ͷ���Ƴ�
        trs.remove(0);
        for (Element tr : trs) {
            Element bookName = tr.child(0);
            Element auth = tr.child(2);
            books.add(new Book(tr.selectFirst("td a").attr("href"), bookName.text(), auth.text()));
        }
        return books;
    }

    /**
     * ��ȡ�½�
     * @param url �鱾��ַ
     * @return html
     */
    public static List<Chapter> getChapter(String url){
        Document doc = Jsoup.parse(sendRequest(url));
        Elements elements = doc.select("#list dl dd a");
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


    /**
     * �����첽����
     * @param url
     * @param callback �ص�����
     */
    public static void sendRequest(String url, Callback callback){
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * ����ͬ������
     * @param url
     */
    public static String sendRequest(String url){
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                MessageDialogBuilder.yesNo("��ʾ", "���س�ʱ��").show();
            }
            if (e instanceof ConnectException || e instanceof UnknownHostException) {
                MessageDialogBuilder.yesNo("��ʾ", "��������ʧ�ܣ�").show();
            }
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
