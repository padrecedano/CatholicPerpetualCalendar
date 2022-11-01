package app.liturgiaplus;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.time.LocalDate;

/**
 *
 * @author cedano
 */
public class Celebration {
    private int id;
    private final int mTime;
    private final int mColor;
    private int psalterWeek;
    private int timeWeek;
    private String mName;
    private final LocalDate mDate;

    public Celebration(int id, LocalDate theDate,String mName, int mTime, int mColor) {
        this.id = id;
        this.mDate=theDate;
        this.mName = mName;
        this.mTime = mTime;
        this.mColor=mColor;
    }

    public Celebration(int id, LocalDate theDate,String mName, int mTime, int mColor, int timeWeek) {
        this.id = id;
        this.mDate=theDate;
        this.mName = mName;
        this.mTime = mTime;
        this.mColor=mColor;
        this.timeWeek=timeWeek;
    }


    public LocalDate getDate() {
        return mDate;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
    public int getColor() {
        return mColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimeWeek(int timeWeek){
        psalterWeek= timeWeek % 4 == 0 ? 4 : timeWeek % 4;
    }

    public int getPsalterWeek(){
        //System.out.println("...."+timeWeek % 4);
        this.psalterWeek=timeWeek % 4 == 0 ? 4 : timeWeek % 4;
        return psalterWeek;
    }

    @Override
    public String toString(){
        return
                String.format("%s - %s - %s PSalter: %d",mDate,this.mName,this.mColor,getPsalterWeek());
    }
}
