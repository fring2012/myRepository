package com.abupdate.iot_sdk;

import com.abupdate.iot_libs.utils.Utils;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author fighter_lee
 * @date 2018/2/28
 */
public class UtilsTest {
    @org.junit.Test
    public void map2String() throws Exception {
        Map map = new HashMap();
        map.put("a",1);
        map.put("b",2);
        map.put("c",3);
        System.out.println(Utils.map2String(map));
        Assert.assertThat(Utils.map2String(map), is("[a:1,b:2,c:3]"));
    }

}