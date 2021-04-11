package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件切割工具类
 * @author ly
 */
public class FileSplitUtils {

    /** 章节正则 */
    private static Pattern p = Pattern.compile("(^\\s*第)(.{1,9})[章节卷集部篇回](\\s{1})(.*)($\\s*)");
    /** 熔断行数 */
    private static int row = 200;
    /** 文件读取器 */
    private static LineIterator lineIterator;
    /** 章节标题 */
    private static String title;
    /** 缓存章节标题 */
    private static String cacheTitle;
    /** 缓存行数 */
    private static String fileLine = "";


    /**
     * 分割本地书籍文件
     * @param book 本地书籍
     * @return 分割后书籍
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
     * 识别章节
     * @return 章节标题
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
     * 读取下一行，跳过空行
     * @return 文件下一行内容
     */
    private static String readline(){
        String line = lineIterator.nextLine().trim();
        return "".equals(line)&&lineIterator.hasNext()?readline():line;
    }
}
