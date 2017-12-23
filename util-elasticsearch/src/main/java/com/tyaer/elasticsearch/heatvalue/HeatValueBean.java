package com.tyaer.elasticsearch.heatvalue;

import java.sql.Timestamp;

/**
 * Created by Twin on 2017/12/15.
 */
public class HeatValueBean {
    private String id;
    private int comments_count;
    private int reposts_count;
    private int zan_count;
    private int read_count;

    private Timestamp create_time;

    private String emotion;
    private String text;
    
    private int heatValue;

    public HeatValueBean() {
    }

    public HeatValueBean(String id, int comments_count, int reposts_count, int zan_count, int read_count, Timestamp create_time, String emotion, String text) {
        this.id = id;
        this.comments_count = comments_count;
        this.reposts_count = reposts_count;
        this.zan_count = zan_count;
        this.read_count = read_count;
        this.create_time = create_time;
        this.emotion = emotion;
        this.text = text;
    }

    public int getHeatValue() {
        return heatValue;
    }

    public void setHeatValue(int heatValue) {
        this.heatValue = heatValue;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getComments_count() {
        return comments_count;
    }

    public void setComments_count(int comments_count) {
        this.comments_count = comments_count;
    }

    public int getReposts_count() {
        return reposts_count;
    }

    public void setReposts_count(int reposts_count) {
        this.reposts_count = reposts_count;
    }

    public int getZan_count() {
        return zan_count;
    }

    public void setZan_count(int zan_count) {
        this.zan_count = zan_count;
    }

    public int getRead_count() {
        return read_count;
    }

    public void setRead_count(int read_count) {
        this.read_count = read_count;
    }

    public Timestamp getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = create_time;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
}
