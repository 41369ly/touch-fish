package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * �ļ��и����
 * @author ly
 */
public class FileSplitUtils {

    /** �½����� */
    private static Pattern p = Pattern.compile("(^\\s*��)(.{1,9})[�½ھ���ƪ��](\\s{1})(.*)($\\s*)");
    /** �۶����� */
    private static int row = 200;
    /** �ļ���ȡ�� */
    private static LineIterator lineIterator;
    /** �½ڱ��� */
    private static String title;
    /** �����½ڱ��� */
    private static String cacheTitle;
    /** �������� */
    private static String fileLine = "";


    /**
     * �ָ���鼮�ļ�
     * @param book �����鼮
     * @return �ָ���鼮
     */
    public static Book split(Book book) {
        File srcFile = new File(book.getUrl());
        String outputDir = srcFile.getPath().substring(0, srcFile.getPath().lastIndexOf('\\'))+"\\temp\\";
        File outputFile = new File(outputDir);
        if (!outputFile.exists()) {
            if (!outputFile.mkdirs()) {
                return null;
            }
        }
        String fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf('.'));
        try {
            lineIterator = FileUtils.lineIterator(srcFile, FileCode.codeString(book.getUrl()));
            for (int i = 0; lineIterator.hasNext(); i++){
                for (int j = 0; j < row&&lineIterator.hasNext(); j++) {
                    if (getChapterTitle()) {
                        break;
                    } else if (j+1 == row&&title==null){
                        title="";
                    }
                }
                if (title!=null&&fileLine.trim().length()>0){
                    String outSrc=outputDir+fileName+i+".txt";
                    FileUtils.writeStringToFile(new File(outSrc),title+"\n"+fileLine.trim(),"UTF-8");
                    book.getChapters().add(new Chapter(outSrc,title));
                }
                title=cacheTitle;
                fileLine="";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * ʶ���½�
     * @return �½ڱ���
     */
    private static boolean getChapterTitle(){
        String line = readline();
        Matcher matcher = p.matcher(line);
        if (matcher.find()){
            cacheTitle = matcher.group();
            return true;
        }
        fileLine +=line+"\n";
        return false;
    }

    /**
     * ��ȡ��һ�У���������
     * @return �ļ���һ������
     */
    private static String readline(){
        String line = lineIterator.nextLine().trim();
        return "".equals(line)&&lineIterator.hasNext()?readline():line;
    }
}
