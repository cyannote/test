package com.cyannote.test;

import java.util.ArrayList;
import java.util.List;

/****
 *  author:tan
 *  data:2019-07-14 19:39
 *  description:
 **/
public class testDemo {
    public static void main(String[] args) {
        List<String> indexUrlList = new ArrayList<String>();
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_star.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_movie.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_tv.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_show.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_music.js?callback=data_callback");

        while (!indexUrlList.isEmpty()) {
            String indexUrl = indexUrlList.remove(0);
            String page = "02";
            while (Integer.parseInt(page) < 30) {
//                String indexUrl = "https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback";
                String[] split = indexUrl.split(".js");
                String s = split[0] + "_" + page + ".js" + split[1];
//        indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_" + page + ".js?callback=data_callback";
                System.out.println(s);

                int pagenum = Integer.parseInt(page);
                pagenum++;
                if (pagenum < 10) {
                    page = "0" + pagenum;
                } else {
                    page = pagenum + "";
                }
            }
        }
    }
}
