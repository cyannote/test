package com.cyannote.spider.tencentNewsSpider;

import com.cyannote.spider.dao.NewsDao;
import com.cyannote.spider.domain.News;
import com.cyannote.spider.utils.HttpClientUtils;
import com.cyannote.spider.utils.IdWorker;
import com.cyannote.spider.utils.JedisUtils;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/****
 *  author:tan
 *  data:2019-07-14 20:17
 *  description:
 **/
public class TencentNewsSpider {
    private static IdWorker idWorker = new IdWorker(0, 2);
    private static NewsDao newsDao = new NewsDao();

    public static void main(String[] args) throws Exception {


        //1.确定首页url
//        String topIndexUrl = "https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=ent&num=60";
        List<String> topIndexUrlList = new ArrayList<String>();
        topIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=ent&num=60");
        topIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=movie&num=60");
        topIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=tv&num=60");
        topIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=137&token=d0f13d594edfc180f5bf6b845456f3ea&ext=music&num=60");
        while (!topIndexUrlList.isEmpty()) {
            String topIndexUrl = topIndexUrlList.remove(0);
            //2.发送请求,获取数据
            String topNewsJsonStr = HttpClientUtils.doGet(topIndexUrl);
            //解析热点数据
            List<News> topNewsList = parseJson(topNewsJsonStr);
            //3.保存数据
            saveNews(topNewsList);
        }

//        String noTopIndexUrl = "https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0";
        List<String> noTopIndexUrlList = new ArrayList<String>();
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=146&token=49cbb2154853ef1a74ff4e53723372ce&ext=ent&page=0");
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=52&token=8f6b50e1667f130c10f981309e1d8200&ext=101&page=0");
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=52&token=8f6b50e1667f130c10f981309e1d8200&ext=102,111,113&page=0");
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=52&token=8f6b50e1667f130c10f981309e1d8200&ext=103&page=0");
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=52&token=8f6b50e1667f130c10f981309e1d8200&ext=105&page=0");
        noTopIndexUrlList.add("https://pacaio.match.qq.com/irs/rcd?cid=52&token=8f6b50e1667f130c10f981309e1d8200&ext=106,118,108&page=0");

        while (!noTopIndexUrlList.isEmpty()) {
            String noTopIndexUrl = noTopIndexUrlList.remove(0);

            int page = 1;
            while (true) {
                //处理非热点数据
                String noTopNewsJsonStr = HttpClientUtils.doGet(noTopIndexUrl);
                //解析数据
                List<News> noTopNewsList = parseJson(noTopNewsJsonStr);
                if (noTopNewsList == null) {
                    break;
                }
                //保存数据
                saveNews(noTopNewsList);
                //获取下一页url
                int index = noTopIndexUrl.lastIndexOf("=");
                noTopIndexUrl = noTopIndexUrl.substring(0,index+1) + page;

                page++;
            }
        }
        System.out.println("本栏目爬取完毕!");
    }

    private static void saveNews(List<News> topNewsList) {
        for (News news : topNewsList) {
            //####################去重########################
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:tencentnews:docurl", news.getDocurl());
            jedis.close();
            if (flag) {
                continue;
            }
            //####################去重########################
            newsDao.saveNews(news);
            //####################去重########################
            jedis = JedisUtils.getJedis();
            jedis.sadd("bigData:spider:tencentnews:docurl", news.getDocurl());
            jedis.close();
            //####################去重########################
        }
    }

    private static List<News> parseJson(String newsJsonStr) {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(newsJsonStr, Map.class);
        Double datanum = (Double) map.get("datanum");
        if (datanum.intValue() == 0) {
            return null;
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<News> list = new ArrayList<News>();
        for (Map<String, Object> newsObject : data) {
//            for (String key : newsObject.keySet()) {
//                System.out.println(key+":"+newsObject.get(key));
            String docurl = (String) newsObject.get("vurl");
            System.out.println(docurl);
            if (docurl.contains("video")) {
                continue;
            }
            //####################去重########################
            Jedis jedis = JedisUtils.getJedis();
            Boolean flag = jedis.sismember("bigData:spider:tencentnews:docurl", docurl);
            jedis.close();
            if (flag) {
                continue;
            }
            //####################去重########################
            //封装news对象
            News news = new News();
            //获取title
            String title = (String) newsObject.get("title");
            news.setTitle(title);
            //获取time
            String time = (String) newsObject.get("update_time");
            news.setTime(time);
            //获取source
            String source = (String) newsObject.get("source");
            news.setSource(source);
            //获取content
            String content = (String) newsObject.get("intro");
            news.setContent(content);
            //获取editor
            news.setEditor(source);
            //获取docurl

            news.setDocurl(docurl);
            //获取id
            long id = idWorker.nextId();
            news.setId(id + "");
            //将news存入list中
            list.add(news);
//            }

        }
        return list;
    }


}
