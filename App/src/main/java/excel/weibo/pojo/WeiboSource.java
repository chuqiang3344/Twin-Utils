package excel.weibo.pojo;

import javax.xml.bind.annotation.XmlSchemaType;
import java.util.Date;

public class WeiboSource {

    @XmlSchemaType(name = "dateTime")
    protected Date addTime;
    @XmlSchemaType(name = "dateTime")
    protected Date assignedTime;
    protected int commentNum;
    protected int commentsCollectedCount;
    protected byte downloadComments;
    protected int downloadSends;
    protected int isOld;
    protected int likeNum;
    protected int pagination;
    @XmlSchemaType(name = "dateTime")
    protected Date publishTime;
    protected int queueType;
    protected int sendNum;
    protected int sendsCollectedCount;
    protected String userId;
    protected String weiboId;
    protected int weiboType;
    protected Integer taskType;
    protected String adCode;

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(Date assignedTime) {
        this.assignedTime = assignedTime;
    }

    /**
     * ��ȡcommentNum���Ե�ֵ��
     */
    public int getCommentNum() {
        return commentNum;
    }

    /**
     * ����commentNum���Ե�ֵ��
     */
    public void setCommentNum(int value) {
        this.commentNum = value;
    }

    /**
     * ��ȡcommentsCollectedCount���Ե�ֵ��
     */
    public int getCommentsCollectedCount() {
        return commentsCollectedCount;
    }

    /**
     * ����commentsCollectedCount���Ե�ֵ��
     */
    public void setCommentsCollectedCount(int value) {
        this.commentsCollectedCount = value;
    }

    /**
     * ��ȡdownloadComments���Ե�ֵ��
     */
    public byte getDownloadComments() {
        return downloadComments;
    }

    /**
     * ����downloadComments���Ե�ֵ��
     */
    public void setDownloadComments(byte value) {
        this.downloadComments = value;
    }

    /**
     * ��ȡdownloadSends���Ե�ֵ��
     */
    public int getDownloadSends() {
        return downloadSends;
    }

    /**
     * ����downloadSends���Ե�ֵ��
     */
    public void setDownloadSends(int value) {
        this.downloadSends = value;
    }


    /**
     * ��ȡisOld���Ե�ֵ��
     */
    public int getIsOld() {
        return isOld;
    }

    /**
     * ����isOld���Ե�ֵ��
     */
    public void setIsOld(int value) {
        this.isOld = value;
    }

    /**
     * ��ȡlikeNum���Ե�ֵ��
     */
    public int getLikeNum() {
        return likeNum;
    }

    /**
     * ����likeNum���Ե�ֵ��
     */
    public void setLikeNum(int value) {
        this.likeNum = value;
    }

    /**
     * ��ȡpagination���Ե�ֵ��
     */
    public int getPagination() {
        return pagination;
    }

    /**
     * ����pagination���Ե�ֵ��
     */
    public void setPagination(int value) {
        this.pagination = value;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    /**
     * ��ȡqueueType���Ե�ֵ��
     */
    public int getQueueType() {
        return queueType;
    }

    /**
     * ����queueType���Ե�ֵ��
     */
    public void setQueueType(int value) {
        this.queueType = value;
    }

    /**
     * ��ȡsendNum���Ե�ֵ��
     */
    public int getSendNum() {
        return sendNum;
    }

    /**
     * ����sendNum���Ե�ֵ��
     */
    public void setSendNum(int value) {
        this.sendNum = value;
    }

    /**
     * ��ȡsendsCollectedCount���Ե�ֵ��
     */
    public int getSendsCollectedCount() {
        return sendsCollectedCount;
    }

    /**
     * ����sendsCollectedCount���Ե�ֵ��
     */
    public void setSendsCollectedCount(int value) {
        this.sendsCollectedCount = value;
    }

    /**
     * ��ȡuserId���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getUserId() {
        return userId;
    }

    /**
     * ����userId���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * ��ȡweiboId���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getWeiboId() {
        return weiboId;
    }

    /**
     * ����weiboId���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWeiboId(String value) {
        this.weiboId = value;
    }

    /**
     * ��ȡweiboType���Ե�ֵ��
     */
    public int getWeiboType() {
        return weiboType;
    }

    /**
     * ����weiboType���Ե�ֵ��
     */
    public void setWeiboType(int value) {
        this.weiboType = value;
    }

}
