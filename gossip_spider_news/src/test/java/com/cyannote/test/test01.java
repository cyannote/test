package com.cyannote.test;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/****
 *  author:tan
 *  data:2019-07-15 9:40
 *  description:
 **/
public class test01 {
    public static void main(String[] args) {
        Random random = new Random();
        int num = 10000000;
//       HashSet<Integer> set = Sets.newHashSetWithExpectedSize(num);
        Set<Integer> set = new HashSet<Integer>();


        long start = System.currentTimeMillis();
        while (set.size()<num){
            int i = random.nextInt(num) + 1;
            set.add(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("用时:"+((end-start)/(double)1000)+"s");
    }
}
