package excel.weibo.pojo;

import java.util.Date;

public class UserSource {
    protected Date addTime;
    protected Date assignedTime;
    protected String city;
    protected Integer concernNum;
    protected int downloadedWeibo;
    protected Integer fansNum;
    protected int onAggregate;
    protected int pagination;
    protected String pid;
    protected String province;
    protected int queueType;
    protected int sumWeibo;
    protected String userId;
    protected Byte userType = 0;//用户类型  普通用户 0/官媒1
    protected String domain;
    protected Integer taskType;
    protected String adCode;

    //new 2017年3月10日 14:16:40
    private int userStatus;// 0:正常  1：用户异常；2：uid不存在
    private String verified_type;//认证类型

    // 2017年7月6日 16:40:17
    private String nickName;
    private String homePageUrl;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getVerified_type() {
        return verified_type;
    }

    public void setVerified_type(String verified_type) {
        this.verified_type = verified_type;
    }

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
     * 获取city属性的值。
     *
     * @return possible object is
     * {@link String }
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置city属性的值。
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCity(String value) {
        this.city = value;
    }

    /**
     * 获取concernNum属性的值。
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getConcernNum() {
        return concernNum;
    }

    /**
     * 设置concernNum属性的值。
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setConcernNum(Integer value) {
        this.concernNum = value;
    }

    /**
     * 获取downloadedWeibo属性的值。
     */
    public int getDownloadedWeibo() {
        return downloadedWeibo;
    }

    /**
     * 设置downloadedWeibo属性的值。
     */
    public void setDownloadedWeibo(int value) {
        this.downloadedWeibo = value;
    }

    /**
     * 获取fansNum属性的值。
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getFansNum() {
        return fansNum;
    }

    /**
     * 设置fansNum属性的值。
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setFansNum(Integer value) {
        this.fansNum = value;
    }

    /**
     * 获取onAggregate属性的值。
     */
    public int getOnAggregate() {
        return onAggregate;
    }

    /**
     * 设置onAggregate属性的值。
     */
    public void setOnAggregate(int value) {
        this.onAggregate = value;
    }

    /**
     * 获取pagination属性的值。
     */
    public int getPagination() {
        return pagination;
    }

    /**
     * 设置pagination属性的值。
     */
    public void setPagination(int value) {
        this.pagination = value;
    }

    /**
     * 获取pid属性的值。
     *
     * @return possible object is
     * {@link String }
     */
    public String getPid() {
        return pid;
    }

    /**
     * 设置pid属性的值。
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPid(String value) {
        this.pid = value;
    }

    /**
     * 获取province属性的值。
     *
     * @return possible object is
     * {@link String }
     */
    public String getProvince() {
        return province;
    }

    /**
     * 设置province属性的值。
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProvince(String value) {
        this.province = value;
    }

    /**
     * 获取queueType属性的值。
     */
    public int getQueueType() {
        return queueType;
    }

    /**
     * 设置queueType属性的值。
     */
    public void setQueueType(int value) {
        this.queueType = value;
    }

    /**
     * 获取sumWeibo属性的值。
     */
    public int getSumWeibo() {
        return sumWeibo;
    }

    /**
     * 设置sumWeibo属性的值。
     */
    public void setSumWeibo(int value) {
        this.sumWeibo = value;
    }

    /**
     * 获取userId属性的值。
     *
     * @return possible object is
     * {@link String }
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置userId属性的值。
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * 获取userType属性的值。
     *
     * @return possible object is
     * {@link Byte }
     */
    public Byte getUserType() {
        return userType;
    }

    /**
     * 设置userType属性的值。
     *
     * @param value allowed object is
     *              {@link Byte }
     */
    public void setUserType(Byte value) {
        this.userType = value;
    }

    @Override
    public String toString() {
        return "UserSource{" +
                "addTime=" + addTime +
                ", assignedTime=" + assignedTime +
                ", city='" + city + '\'' +
                ", concernNum=" + concernNum +
                ", downloadedWeibo=" + downloadedWeibo +
                ", fansNum=" + fansNum +
                ", onAggregate=" + onAggregate +
                ", pagination=" + pagination +
                ", pid='" + pid + '\'' +
                ", province='" + province + '\'' +
                ", queueType=" + queueType +
                ", sumWeibo=" + sumWeibo +
                ", userId='" + userId + '\'' +
                ", userType=" + userType +
                ", domain='" + domain + '\'' +
                ", taskType=" + taskType +
                ", adCode='" + adCode + '\'' +
                ", verified_type='" + verified_type + '\'' +
                '}';
    }
}
