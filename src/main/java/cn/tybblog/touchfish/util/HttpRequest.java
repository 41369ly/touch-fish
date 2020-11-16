package cn.tybblog.touchfish.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpRequest {
    /**
     * ��ָ��URL����GET����������
     *
     * @param url   ���������URL
     * @param param ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return URL ������Զ����Դ����Ӧ���
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            if (param != null && !"".equals(param)) {
                urlNameString += "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            // �򿪺�URL֮�������
            URLConnection connection = realUrl.openConnection();
            // ����ͨ�õ���������
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // ����ʵ�ʵ�����
            connection.connect();
            // ��ȡ������Ӧͷ�ֶ�
            Map<String, List<String>> map = connection.getHeaderFields();
            // �������е���Ӧͷ�ֶ�
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // ���� BufferedReader����������ȡURL����Ӧ
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("����GET��������쳣��" + e);
            e.printStackTrace();
        }
        // ʹ��finally�����ر�������
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * ��ָ�� URL ����POST����������
     *
     * @param url   ��������� URL
     * @param param ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return ������Զ����Դ����Ӧ���
     */
    public static String sendPost(String url, String param) {
        StringBuffer sb = new StringBuffer("");
        HttpPost post = new HttpPost(url);
        post.setHeader("Connection", "keep-alive");
        List<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("searchkey", param));
        try {
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpClient httpClient = new DefaultHttpClient();
        BufferedReader in = null;
        try {
            HttpResponse response = httpClient.execute(post);
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {    //����ɹ�
                in = new BufferedReader(new InputStreamReader(response.getEntity()
                        .getContent(), "utf-8"));
                String line = "";
                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }

                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String sendZipGet(String url) throws IOException {
        StringBuffer sb = new StringBuffer("");
        HttpGet get = new HttpGet(url);
        get.setHeader("Connection", "keep-alive");
        get.setHeader("Accept-Encoding", "gzip");
        HttpClient httpClient = new DefaultHttpClient();
        BufferedReader in = null;

        HttpResponse response = httpClient.execute(get);
        int code = response.getStatusLine().getStatusCode();
        if (code == 200) {    //����ɹ�
            InputStreamReader isr = new InputStreamReader(new GZIPInputStream(response.getEntity().getContent()), "UTF-8");
            in = new BufferedReader(isr);
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }

            in.close();
        }
        return sb.toString();
    }
}