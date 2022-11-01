package app.liturgiaplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        setData();
    }

    public static void setData() {
        HashMap<String, Boolean> mMapA=new HashMap();
        mMapA.put("EpiphanyOnSunday",true);
        HashMap<String, Boolean> mMapB=new HashMap();
        mMapB.put("EpiphanyOnSunday",false);
        //mMapB.put("ImmaculatePrevails",false);
        mMapB.put("AscensionOriginal",true);
        //mMapB.put("CorpusOriginal",true);
        LiturgicalCalendar.generateYearlyCalendar(2022,mMapB);
        //System.out.println("\n---\n----- 2022 -----\n---");
        //LiturgicalCalendar.generateYearlyCalendar(2022);
        LiturgicalCalendar.printCalendar();
        //listView.setItems(items);

        //System.out.println(LiturgicalCalendar.getCalendar().get(0).getName());
        //System.out.println(LiturgicalCalendar.getAdventTest3(2022).toString());
        //LiturgicalCalendar.getAdventTest(2022).toString();
/*
        LiturgicalCalendar.getAdventTest(2016).toString();
        LiturgicalCalendar.getAdventTest(2017).toString();
        LiturgicalCalendar.getAdventTest(2018).toString();
        LiturgicalCalendar.getAdventTest(2019).toString();
        LiturgicalCalendar.getAdventTest(2020).toString();
        LiturgicalCalendar.getAdventTest(2021).toString();
        LiturgicalCalendar.getAdventTest(2022).toString();
        LiturgicalCalendar.getAdventTest(2023).toString();
*/
    }

}