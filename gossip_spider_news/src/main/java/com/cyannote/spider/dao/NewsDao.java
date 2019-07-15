package com.cyannote.spider.dao;

import com.cyannote.spider.domain.News;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

/****
 *  author:tan
 *  data:2019-07-14 18:39
 *  description:
 **/
public class NewsDao extends JdbcTemplate{
    private static ComboPooledDataSource dataSource = new ComboPooledDataSource();
    static {
        try {
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setJdbcUrl("jdbc:mysql:///gossip");
            dataSource.setUser("root");
            dataSource.setPassword("abc");
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public NewsDao(){
        super.setDataSource(dataSource);
    }

    public void saveNews(News news){
        String[] params = {news.getId(),news.getTitle(),news.getTime(),news.getSource(),news.getContent(),news.getEditor(),news.getDocurl()};
        update("INSERT INTO news VALUES (?,?,?,?,?,?,?)",params);
    }

}
