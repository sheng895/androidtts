package com.gykj.paddle.lite.demo.tts;

import android.content.res.AssetManager;

import com.huaban.analysis.jieba.JiebaSegmenter;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汉字转为拼音
 */
public class ConvertPinyin {
    static String keyWord = "：，；。？！“”‘’':,;.?!";
    static String filterWord = "y";

    public static  AssetManager assetManager;

    public static  JiebaSegmenter jiebaSegmenter;



    /**
     * 汉字转全拼
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static List<String> getPinyin(String str, Map<String, String> pinyinmap) {
        List<String> list = new ArrayList<>();
        if (str == null || str.length() == 0) {
            return list;
        }
        char[] t1 = null;
        t1 = str.toCharArray();
        String[] t2 = new String[t1.length];
        // 设置汉字拼音输出的格式
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写
        t3.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);// 不带声调
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);

        int t0 = t1.length;

        for (int i = 0; i < t0; i++) {
            // 判断是否为汉字字符
            if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                // 提取汉字的首字母
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    if (pinyinArray != null) {
                        String word = pinyinArray[0];
                        word = word.replace("u:", "v");
                        if (pinyinmap.get(word) != null) {
                            String[] initial_final_list = pinyinmap.get(word).split(" ");

                            if (initial_final_list.length == 2) {
                                list.add(initial_final_list[0]);
                                list.add(initial_final_list[1]);
                            }
                            if (initial_final_list.length == 1) {
                                list.add("");
                                list.add(initial_final_list[0]);
                            }
//                        String firstChar=word.substring(0,1);
//                        if (word.startsWith(firstChar))
//                        {
//                            if (word.startsWith(firstChar+"h"))
//                            {
//                                list.add(word.substring(0, 2));
//                                list.add(word.substring(2, word.length()));
//                            }
//                            else {
//                                if (word.length() > 1) {
//                                    if (!filterWord.contains(firstChar)) {
//                                        list.add(firstChar);
//                                    }
//                                    list.add(word.substring(1, word.length()));
//                                }
//                            }
//                        }
//                        else
//                        {
//                            list.add(word);
//                        }
                            // 取出该汉字全拼的第一种读音并连接到字符串t4后
//                    }
//                    } else {
//                        convert += word;
//                    }
                            // 将汉字的几种全拼都存到t2数组中

                        } else {
                            // 如果不是汉字字符，直接取出字符并连接到字符串t4后
                            if (keyWord.contains(Character.toString(t1[i])))
                                list.add("sp");
                            else
                                list.add(Character.toString(t1[i]));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
        return list;
    }

    /**
     * 汉字转简拼
     *
     * @param str
     * @return String
     */
    public static String getPinYinHeadChar(String str) {
        String convert = "";
        if (str == null || str.length() == 0) {
            return convert;
        }
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            // 提取汉字的首字母
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert.toUpperCase();
    }

    /**
     * 返回对应拼音编码
     *
     * @param str
     * @param map
     * @return
     */
    public static float[] getPinyinPhone(String str, Map<String, String> map, Map<String, String> pinyinmap) {
       //进行断句分词
        List<String> jiebaword = jiebaSegmenter.sentenceProcess(str);
        List<String> totalwordpinyinglist=new ArrayList<>();
        List<String> pinyinglist=new ArrayList<>();
         Converter converter = new PinyinConverter(assetManager);

        for (String word :jiebaword) {
            List<String> wordPinyinList = converter.getPinyin(word);
            totalwordpinyinglist.addAll(wordPinyinList);
//            List<String> pinyinlist = ConvertPinyin.getPinyin(str, pinyinmap);

        }

        for (String pinyin :totalwordpinyinglist) {
            pinyin = pinyin.replace("u:", "v");
            if (pinyinmap.get(pinyin) != null) {
                String[] initial_final_list = pinyinmap.get(pinyin).split(" ");
                if (initial_final_list.length == 2) {
                    if (!initial_final_list[0].equals("")) {
                        pinyinglist.add(initial_final_list[0]);
                    }
                    pinyinglist.add(initial_final_list[1]);
                }
                if (initial_final_list.length == 1) {
//                    pinyinglist.add("");
                    pinyinglist.add(initial_final_list[0]);
                }
            }
            else
            {
                pinyinglist.add(pinyin);
            }
        }


        float[] phones = new float[pinyinglist.size()];
        int index = 0;
        for (String item : pinyinglist) {
            String KeyValue = map.get(item) == null ? "" : map.get(item).toString();
            if (KeyValue != "") {
                float ft = Float.valueOf(KeyValue);
                phones[index] = ft;
                index++;
            } else {
                String dddd = item;
                index++;
            }
        }

        return phones;
    }


    public static Map<String, String> readFileMap(String filename,  String spilt) {
        Map<String, String> map = new HashMap<>();
        try {
            InputStream is = assetManager.open(filename);
            BufferedReader buffreader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            String[] linevalue;
            try {
                while ((line = buffreader.readLine()) != null) {
                    linevalue = line.split(spilt);
                    map.put(linevalue[0], linevalue[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }


}
