package excel.weibo.pojo;

public class KeywordSource {

    protected String collectionUrl;
    protected String countScope;
    protected String id;
    protected byte paged;
    protected byte pagination;
    protected String sourceData;
    protected int sumPage;
    protected byte type;
    protected Integer taskType;

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    /**
     * ��ȡcollectionUrl���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getCollectionUrl() {
        return collectionUrl;
    }

    /**
     * ����collectionUrl���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCollectionUrl(String value) {
        this.collectionUrl = value;
    }

    /**
     * ��ȡcountScope���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getCountScope() {
        return countScope;
    }

    /**
     * ����countScope���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCountScope(String value) {
        this.countScope = value;
    }

    /**
     * ��ȡid���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * ����id���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * ��ȡpaged���Ե�ֵ��
     */
    public byte getPaged() {
        return paged;
    }

    /**
     * ����paged���Ե�ֵ��
     */
    public void setPaged(byte value) {
        this.paged = value;
    }

    /**
     * ��ȡpagination���Ե�ֵ��
     */
    public byte getPagination() {
        return pagination;
    }

    /**
     * ����pagination���Ե�ֵ��
     */
    public void setPagination(byte value) {
        this.pagination = value;
    }

    /**
     * ��ȡsourceData���Ե�ֵ��
     *
     * @return possible object is
     * {@link String }
     */
    public String getSourceData() {
        return sourceData;
    }

    /**
     * ����sourceData���Ե�ֵ��
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSourceData(String value) {
        this.sourceData = value;
    }

    /**
     * ��ȡsumPage���Ե�ֵ��
     */
    public int getSumPage() {
        return sumPage;
    }

    /**
     * ����sumPage���Ե�ֵ��
     */
    public void setSumPage(int value) {
        this.sumPage = value;
    }

    /**
     * ��ȡtype���Ե�ֵ��
     */
    public byte getType() {
        return type;
    }

    /**
     * ����type���Ե�ֵ��
     */
    public void setType(byte value) {
        this.type = value;
    }

}
