package com.leyou.item.pojo;

import javax.persistence.*;
import java.util.Date;

@Table(name = "tb_sku")
public class Sku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long spuId;
    private String title;
    private String images;
    private Long price;
    private String ownSpec;// 商品特殊规格的键值对
    private String indexes;// 商品特殊规格的下标
    private Boolean enable;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间
    private Date lastUpdateTime;// 最后修改时间
    @Transient
    private Integer stock;// 库存

    public void setId(Long id) {
        this.id = id;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public void setOwnSpec(String ownSpec) {
        this.ownSpec = ownSpec;
    }

    public void setIndexes(String indexes) {
        this.indexes = indexes;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public Long getSpuId() {
        return spuId;
    }

    public String getTitle() {
        return title;
    }

    public String getImages() {
        return images;
    }

    public Long getPrice() {
        return price;
    }

    public String getOwnSpec() {
        return ownSpec;
    }

    public String getIndexes() {
        return indexes;
    }

    public Boolean getEnable() {
        return enable;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Integer getStock() {
        return stock;
    }
}
