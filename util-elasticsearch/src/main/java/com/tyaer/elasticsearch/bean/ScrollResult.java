package com.tyaer.elasticsearch.bean;

/**
 * Created by Twin on 2017/12/5.
 */
public class ScrollResult {
    private String scroll_id;
    private String result;

    public ScrollResult() {
    }

    public ScrollResult(String scroll_id, String result) {
        this.scroll_id = scroll_id;
        this.result = result;
    }

    @Override
    public String toString() {
        return "ScrollResult{" +
                "scroll_id='" + scroll_id + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public String getScroll_id() {
        return scroll_id;
    }

    public void setScroll_id(String scroll_id) {
        this.scroll_id = scroll_id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
