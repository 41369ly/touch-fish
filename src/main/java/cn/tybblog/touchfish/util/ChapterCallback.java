package cn.tybblog.touchfish.util;

import cn.tybblog.touchfish.exception.FishException;

import java.util.List;

/**
 * @author ly
 */
public interface ChapterCallback {
    /**
     * �ص�
     * @param bookText �鱾����
     * @param baseMethod ��Դ����
     */
    void chapter(List<String> bookText,String baseMethod) throws FishException;
}
