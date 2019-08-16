package com.xgoing.ftp;

import java.util.Arrays;

public class Test {

    @org.testng.annotations.Test
    public void testPattern() {
        String PATH_PAT = ".*([\\\\|/]+).*";
        String target = "/\\test///in\\\\";
        System.out.println(target.matches(PATH_PAT));
        String rest = target.replaceAll("\\\\+", "/").replaceAll("/+", "/");
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }

        if (rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        System.out.println(rest);


        String[] arr = "/in/".split("/");
        System.out.println(Arrays.toString(arr));
    }
}
