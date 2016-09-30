package dp.beans.category;

import java.util.Date;

public class CategoryPartDO {
    private Integer id;

    private Date gmtCreate;

    private Date gmtModified;

    private String isDeleted;

    private String partName;

    private String partCode;

    private Integer firstCatId;

    private String firstCatName;

    private Integer secondCatId;

    private String secondCatName;

    private Integer thirdCatId;

    private String thirdCatName;

    private String sumCode;

    private String alissNameText;

    private String labelNameText;

    private Integer catKind;

    private String vehicleCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted == null ? null : isDeleted.trim();
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName == null ? null : partName.trim();
    }

    public String getPartCode() {
        return partCode;
    }

    public void setPartCode(String partCode) {
        this.partCode = partCode == null ? null : partCode.trim();
    }

    public Integer getFirstCatId() {
        return firstCatId;
    }

    public void setFirstCatId(Integer firstCatId) {
        this.firstCatId = firstCatId;
    }

    public String getFirstCatName() {
        return firstCatName;
    }

    public void setFirstCatName(String firstCatName) {
        this.firstCatName = firstCatName == null ? null : firstCatName.trim();
    }

    public Integer getSecondCatId() {
        return secondCatId;
    }

    public void setSecondCatId(Integer secondCatId) {
        this.secondCatId = secondCatId;
    }

    public String getSecondCatName() {
        return secondCatName;
    }

    public void setSecondCatName(String secondCatName) {
        this.secondCatName = secondCatName == null ? null : secondCatName.trim();
    }

    public Integer getThirdCatId() {
        return thirdCatId;
    }

    public void setThirdCatId(Integer thirdCatId) {
        this.thirdCatId = thirdCatId;
    }

    public String getThirdCatName() {
        return thirdCatName;
    }

    public void setThirdCatName(String thirdCatName) {
        this.thirdCatName = thirdCatName == null ? null : thirdCatName.trim();
    }

    public String getSumCode() {
        return sumCode;
    }

    public void setSumCode(String sumCode) {
        this.sumCode = sumCode == null ? null : sumCode.trim();
    }

    public String getAlissNameText() {
        return alissNameText;
    }

    public void setAlissNameText(String alissNameText) {
        this.alissNameText = alissNameText == null ? null : alissNameText.trim();
    }

    public String getLabelNameText() {
        return labelNameText;
    }

    public void setLabelNameText(String labelNameText) {
        this.labelNameText = labelNameText == null ? null : labelNameText.trim();
    }

    public Integer getCatKind() {
        return catKind;
    }

    public void setCatKind(Integer catKind) {
        this.catKind = catKind;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode == null ? null : vehicleCode.trim();
    }
}