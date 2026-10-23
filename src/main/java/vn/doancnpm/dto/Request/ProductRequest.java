package vn.doancnpm.dto.Request;

import java.math.BigDecimal;

public class ProductRequest {

    private String name;
    private Long categoryId;
    private String imgURL;
    private BigDecimal unitPrice;
    private Boolean status;    
    private Long version;

    public ProductRequest() {
    }

    public ProductRequest(String name, Long categoryId, String imgURL,
                          BigDecimal unitPrice, Boolean status, Long version) {
        this.name = name;
        this.categoryId = categoryId;
        this.imgURL = imgURL;
        this.unitPrice = unitPrice;
        this.status = status;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Long getVersion(){
        return version;
    }

    public void setVersion(Long version){
        this.version = version;
    }
}