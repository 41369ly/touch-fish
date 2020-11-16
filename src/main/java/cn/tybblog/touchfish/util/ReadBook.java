package cn.tybblog.touchfish.util;

public interface ReadBook {
    /**
     * 上一章
     */
    void preChapter();
    /**
     * 下一章
     */
    void nextChapter();
    /**
     * 初始化
     */
    void info();
    /**
     * 下一行
     */
    void nextInfo();
    /**
     * 上一行
     */
    void preInfo();
}
