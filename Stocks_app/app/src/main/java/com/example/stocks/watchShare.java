package com.example.stocks;

public class watchShare {
    private String ticker;
    private String subTitle;
    private Float last;
    private Float change;
    private String changeColor;



    public void setTicker(String ticker) {
        this.ticker=ticker;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setLast(Float last) {
        this.last = last;
    }

    public void setChange(Float change) {
        this.change = change;
    }

    public void setChangeColor(String changeColor) {
        this.changeColor = changeColor;
    }



    public String getTicker() {
        return ticker;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public Float getLast() {
        return last;
    }

    public Float getChange() {
        return change;
    }

    public String getChangeColor() {
        return changeColor;
    }

}
