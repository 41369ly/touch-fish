package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.entity.Book;
import cn.tybblog.touchfish.entity.Chapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 文件切割工具类
 */
public class FileSplitUtils {

    /**
     * 默认切割大小
     */
    public static final int DEFAULT_SIZE = 1024 * 100;

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
            FileInputStream inputStream = FileUtils.openInputStream(srcFile);
            File temp = null;
            byte[] buffer = new byte[DEFAULT_SIZE];
            int len = 0;
            for (int i = 0; (len = IOUtils.read(inputStream, buffer)) > 0; i++) {
                Chapter character=new Chapter(outputDir+fileName+i+".txt","");
                book.getChapters().add(character);
                temp = FileUtils.getFile(outputDir, fileName+i+".txt");
                FileUtils.writeByteArrayToFile(temp, buffer, 0, len);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return book;
    }
}
