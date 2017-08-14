package excel.weibo.pojo;

public class WeiboResult {
    private String mid;//微博id
    private String mid_p;//转发的微博id
    private String source;//微博来源
    private String created_at;//发表时间戳,注册时间
    private String uid;//用户id
    private String tel;
    private int comments_count = 0;//评论数
    private int reposts_count = 0;//转发数
    private int zan_count = 0;//点赞数
    private String name;//作者昵称
    private String text;//微博内容
    private String pic;//图片
    private String url;//url
    private int followers_count = 0;//粉丝数
    private int friends_count = 0;//关注数
    private int statuses_count = 0;//微博数
    private String favourites_count;//收藏数
    private String biFollowersCount;//双向粉丝数
    private String verified;//是否认证
    private String verified_type;//认证类型
    private String verified_reason;//认证原因
    private String bagde_num;//勋章数
    private String level_info;//等级信息
    private String trust_ifo;//信用等级
    private String user_url;//用户地址
    private String profileImageUrl;//用户头像
    private String domain;//个性域名
    private String gender;//性别
    private String province;//省份
    private String city;//城市
    private String birthday;//生日
    private String job_info;//职业信息
    private String taglist;//标签信息
    private String address;//地址
    private String oid;//关注的用户id
    private String description;//个人说明
    private String text_subject;//话题
    private String at_who;//@谁
    private String type;
    private String site_id;//解析类型，0新浪/腾讯等
    private String html;
    private String emotion_tag;
    private String love;//感情状况
    private String email;//邮箱
    private String edu_info;//教育信息
    private String blood;//血型
    private String sextrend;//性取向
    private String qq;//QQ
    private String blog;//博客地址
    private String msn;//MSN
    private String member_info;//会员等级
    private int isRepeat;
    /**
     * 会员信息
     */
    private String bagde_info;//勋章信息
    private int isYearVip;//是否为年费会员
    private int experience;//经验值
    private int growthSpeed;//成长速度
    private int growthValue;//成长值
    private String creditDegree;//信用程度
    private String domainId;
    private String industryType;

    public static void main(String[] args) {
        WeiboResult weiboResult = new WeiboResult();
        System.out.println(weiboResult.getIsRepeat());
        System.out.println(weiboResult.getAt_who());
    }

    @Override
    public String toString() {
        return "WeiboResult{" +
                "mid='" + mid + '\'' +
                ", mid_p='" + mid_p + '\'' +
                ", source='" + source + '\'' +
                ", created_at='" + created_at + '\'' +
                ", uid='" + uid + '\'' +
                ", tel='" + tel + '\'' +
                ", comments_count=" + comments_count +
                ", reposts_count=" + reposts_count +
                ", zan_count=" + zan_count +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", pic='" + pic + '\'' +
                ", url='" + url + '\'' +
                ", followers_count=" + followers_count +
                ", friends_count=" + friends_count +
                ", statuses_count=" + statuses_count +
                ", favourites_count='" + favourites_count + '\'' +
                ", biFollowersCount='" + biFollowersCount + '\'' +
                ", verified='" + verified + '\'' +
                ", verified_type='" + verified_type + '\'' +
                ", verified_reason='" + verified_reason + '\'' +
                ", bagde_num='" + bagde_num + '\'' +
                ", level_info='" + level_info + '\'' +
                ", trust_ifo='" + trust_ifo + '\'' +
                ", user_url='" + user_url + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", domain='" + domain + '\'' +
                ", gender='" + gender + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", birthday='" + birthday + '\'' +
                ", job_info='" + job_info + '\'' +
                ", taglist='" + taglist + '\'' +
                ", address='" + address + '\'' +
                ", oid='" + oid + '\'' +
                ", description='" + description + '\'' +
                ", text_subject='" + text_subject + '\'' +
                ", at_who='" + at_who + '\'' +
                ", type='" + type + '\'' +
                ", site_id='" + site_id + '\'' +
                ", html='" + html + '\'' +
                ", emotion_tag='" + emotion_tag + '\'' +
                ", love='" + love + '\'' +
                ", email='" + email + '\'' +
                ", edu_info='" + edu_info + '\'' +
                ", blood='" + blood + '\'' +
                ", sextrend='" + sextrend + '\'' +
                ", qq='" + qq + '\'' +
                ", blog='" + blog + '\'' +
                ", msn='" + msn + '\'' +
                ", member_info='" + member_info + '\'' +
                ", isRepeat=" + isRepeat +
                ", bagde_info='" + bagde_info + '\'' +
                ", isYearVip=" + isYearVip +
                ", experience=" + experience +
                ", growthSpeed=" + growthSpeed +
                ", growthValue=" + growthValue +
                ", creditDegree='" + creditDegree + '\'' +
                ", domainId='" + domainId + '\'' +
                ", industryType='" + industryType + '\'' +
                '}';
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getCreditDegree() {
        return creditDegree;
    }

    public void setCreditDegree(String creditDegree) {
        this.creditDegree = creditDegree;
    }

    public int getIsYearVip() {
        return isYearVip;
    }

    public void setIsYearVip(int isYearVip) {
        this.isYearVip = isYearVip;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getGrowthValue() {
        return growthValue;
    }

    public void setGrowthValue(int growthValue) {
        this.growthValue = growthValue;
    }

    public int getGrowthSpeed() {
        return growthSpeed;
    }

    public void setGrowthSpeed(int growthSpeed) {
        this.growthSpeed = growthSpeed;
    }

    public String getBagde_info() {
        return bagde_info;
    }

    public void setBagde_info(String bagde_info) {
        this.bagde_info = bagde_info;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getMid_p() {
        return mid_p;
    }

    public void setMid_p(String mid_p) {
        this.mid_p = mid_p;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public int getFriends_count() {
        return friends_count;
    }

    public void setFriends_count(int friends_count) {
        this.friends_count = friends_count;
    }

    public int getStatuses_count() {
        return statuses_count;
    }

    public void setStatuses_count(int statuses_count) {
        this.statuses_count = statuses_count;
    }

    public String getFavourites_count() {
        return favourites_count;
    }

    public void setFavourites_count(String favourites_count) {
        this.favourites_count = favourites_count;
    }

    public String getBiFollowersCount() {
        return biFollowersCount;
    }

    public void setBiFollowersCount(String biFollowersCount) {
        this.biFollowersCount = biFollowersCount;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getVerified_type() {
        return verified_type;
    }

    public void setVerified_type(String verified_type) {
        this.verified_type = verified_type;
    }

    public String getBagde_num() {
        return bagde_num;
    }

    public void setBagde_num(String bagde_num) {
        this.bagde_num = bagde_num;
    }

    public String getLevel_info() {
        return level_info;
    }

    public void setLevel_info(String level_info) {
        this.level_info = level_info;
    }

    public String getTrust_ifo() {
        return trust_ifo;
    }

    public void setTrust_ifo(String trust_ifo) {
        this.trust_ifo = trust_ifo;
    }

    public String getVerified_reason() {
        return verified_reason;
    }

    public void setVerified_reason(String verified_reason) {
        this.verified_reason = verified_reason;
    }

    public String getUser_url() {
        return user_url;
    }

    public void setUser_url(String user_url) {
        this.user_url = user_url;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getJob_info() {
        return job_info;
    }

    public void setJob_info(String job_info) {
        this.job_info = job_info;
    }

    public String getTaglist() {
        return taglist;
    }

    public void setTaglist(String taglist) {
        this.taglist = taglist;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText_subject() {
        return text_subject;
    }

    public void setText_subject(String text_subject) {
        this.text_subject = text_subject;
    }

    public String getAt_who() {
        return at_who;
    }

    public void setAt_who(String at_who) {
        this.at_who = at_who;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSite_id() {
        return site_id;
    }

    public void setSite_id(String site_id) {
        this.site_id = site_id;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getEmotion_tag() {
        return emotion_tag;
    }

    public void setEmotion_tag(String emotion_tag) {
        this.emotion_tag = emotion_tag;
    }

    public String getLove() {
        return love;
    }

    public void setLove(String love) {
        this.love = love;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEdu_info() {
        return edu_info;
    }

    public void setEdu_info(String edu_info) {
        this.edu_info = edu_info;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getSextrend() {
        return sextrend;
    }

    public void setSextrend(String sextrend) {
        this.sextrend = sextrend;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public String getMsn() {
        return msn;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public String getMember_info() {
        return member_info;
    }

    public void setMember_info(String member_info) {
        this.member_info = member_info;
    }

    public int getIsRepeat() {
        return isRepeat;
    }

    public void setIsRepeat(int isRepeat) {
        this.isRepeat = isRepeat;
    }

}
