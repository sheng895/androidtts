package com.gykj.paddle.lite.demo.tts;

import java.util.List;

/**
 * @author Ricky Fung
 */
public interface Converter {

    String[] getPinyin(char ch) ;

    List<String> getPinyin(String chinese) ;
}
