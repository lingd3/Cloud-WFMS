package org.sysu.activitiservice.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class CommonUtil {

    //获取全部的列表元素,以split符号隔开
    public static String ArrayList2String(ArrayList<String> arrayList, String split) {
        StringBuilder sb = new StringBuilder();
        for(String s : arrayList) {
            sb.append(s).append(split);
        }
        return sb.toString();
    }
}