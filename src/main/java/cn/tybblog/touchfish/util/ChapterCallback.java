package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.exception.FishException;

import java.util.List;

/**
 * @author ly
 */
public interface ChapterCallback {
    /**
     * 回调
     * @param bookText 书本内容
     * @param baseMethod 来源方法
     */
    void chapter(List<String> bookText,String baseMethod) throws FishException;
}
