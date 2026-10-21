package vn.doancnpm.dto.Request;

import java.math.BigDecimal;

public class ProductRequest {

    private String name;
    private Long categoryId;
    //private String imgURL;
    private BigDecimal unitPrice;
    private Integer stock;      // không gửi thì mặc định 0 khi tạo mới
    private Boolean status;     // true = đang bán, không gửi thì mặc định true khi tạo mới

    public ProductRequest() {
    }

    public ProductRequest(String name, Long categoryId, //String imgURL,
                          BigDecimal unitPrice, Integer stock, Boolean status) {
        this.name = name;
        this.categoryId = categoryId;
        //this.imgURL = imgURL;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.status = status;
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

    //public String getImgURL() {
    //  return imgURL;
    //}

    //public void setImgURL(String imgURL) {
    //    this.imgURL = imgURL;
    //}

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}