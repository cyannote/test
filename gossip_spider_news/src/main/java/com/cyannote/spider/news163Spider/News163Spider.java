package com.cyannote.spider.news163Spider;

import com.cyannote.spider.dao.NewsDao;
import com.cyannote.spider.domain.News;
import com.cyannote.spider.utils.HttpClientUtils;
import com.cyannote.spider.utils.IdWorker;
import com.cyannote.spider.utils.JedisUtils;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/****
 *  author:tan
 *  data:2019-07-14 13:08
 *  description:
 **/
public class News163Spider {
    private static IdWorker idWorker = new IdWorker(0,1);
    private static NewsDao newsDao = new NewsDao();

    public static void main(String[] args) throws Exception {

        //1.确定首页url
//        String indexUrl = "https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback";
        List<String> indexUrlList = new ArrayList<String>();
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_index.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_star.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_movie.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_tv.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_show.js?callback=data_callback");
        indexUrlList.add("https://ent.163.com/special/000380VU/newsdata_music.js?callback=data_callback");

        while (!indexUrlList.isEmpty()) {
            String indexUrl = indexUrlList.remove(0);


            page(indexUrl);
            System.out.println("本栏数据爬取完毕");
        }
    }

    private static void page(String indexUrl) throws IOException {
        String page = "02";
        while (true) {
            //2.发送请求,获取数据
            String jsonStr = HttpClientUtils.doGet(indexUrl);
            if (jsonStr == null) {
                break;
            }
            //处理Json数据
            jsonStr = splitJson(jsonStr);
//        System.out.println(jsonStr);
            //3.解析数据
            parseJson(jsonStr);
            //5.获取下一页的数据

//                indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_" + page + ".js?callback=data_callback";
           /* if (indexUrl.s)
            String[] split = indexUrl.split(".js");
            indexUrl = split[0] + "_" + page + ".js" + split[1];*/
            //4. 获取下一页的url
            if(indexUrl.contains("newsdata_index")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_index_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_star")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_star_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_movie")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_movie_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_tv")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_tv_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_show")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_show_" + page + ".js?callback=data_callback";
            }
            if(indexUrl.contains("newsdata_music")){
                indexUrl = "https://ent.163.com/special/000380VU/newsdata_music_" + page + ".js?callback=data_callback";
            }
            int pagenum = Integer.parseInt(page);
            pagenum++;
            if (pagenum < 10) {
                page = "0" + pagenum;
            } else {
                page = pagenum + "";
            }
        }
    }

    private static void parseJson(String jsonStr) throws IOException {
        Gson gson = new Gson();
        //将json数据转换为list对象
        List<Map<String, Object>> newsList = gson.fromJson(jsonStr, List.class);
        for (Map<String, Object> newsObject : newsList) {
            //获取详情页url
            String docurl = (String) newsObject.get("docurl");
            if (!docurl.contains("ent.163.com") || docurl.contains("photoview")) {
                continue;
            }
//            System.out.println(docurl);
            //##############去重##############
            //判断docurl是否在redis中存在
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:163News:docurl", docurl);
            jedis.close();
            if (flag) {
                continue;
            }
            //##############去重##############
            //获取新闻详情页的数据
            News news = prseNewsItem(docurl);
            //4.保存数据
            newsDao.saveNews(news);
            //##############去重##############
            //docurl保存到redis
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:163News:docurl", docurl);
            jedis.close();
            //##############去重##############

        }

    }

    private static News prseNewsItem(String docurl) throws IOException {

        //获取详情页数据
        String html = HttpClientUtils.doGet(docurl);
        //解析数据
        Document document = Jsoup.parse(html);
        News news = new News();
        //获取title
        Elements h1El = document.select("#epContentLeft h1");
        String title = h1El.text();
        news.setTitle(title);
        //获取时间
        Elements seEl = document.select(".post_time_source");
        String[] split = seEl.text().split("　来源: ");
        /*for (String s : split) {
            System.out.println(s);
        }*/
        news.setTime(split[0]);
//        System.out.println(split[1]);
        //获取来源
        news.setSource(split[1].split(" ")[0]);
        //获取内容
        String content = document.select("#endText p").text();
        news.setContent(content);
        //获取编辑
        String editor = document.select(".ep-editor").text();
        editor = editor.substring(editor.indexOf("：") + 1, editor.lastIndexOf("_"));
        news.setEditor(editor);
        //获取id
        long id = idWorker.nextId();
        news.setId(id + "");
        //存入docUrl
        news.setDocurl(docurl);
//        System.out.println(news);
        return news;
    }

    private static String splitJson(String jsonStr) {
        int firstIndex = jsonStr.indexOf("(");
        int lastIndex = jsonStr.lastIndexOf(")");
        return jsonStr.substring(firstIndex + 1, lastIndex);
    }
}
