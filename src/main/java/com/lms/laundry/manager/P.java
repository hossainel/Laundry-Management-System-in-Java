package com.lms.laundry.manager;

public class P {
    public static void p(String s){ System.out.print(s); }
    public static void pf(String s){ System.out.println(s); }
    public static void pf(Object s){ System.out.println(s); }
    public static void pf(){ System.out.println(); }
    public static void pd(String s) {
        String e = "=".repeat(s.length()+4);
        pf(e); pf("= "+s+" ="); pf(e);
    }
}
