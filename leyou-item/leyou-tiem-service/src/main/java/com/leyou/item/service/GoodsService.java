package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 根据条件分页查询Spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //添加查询条件
        if (StringUtils.isNotBlank(key)){
             criteria.andLike("title","%"+key+"%");
        }

        //添加上下架的过滤条件
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }

        //添加分页
        PageHelper.startPage(page,rows);
        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> info = new PageInfo<>(spus);
        //spu集合要转化成spubo集合
        List<SpuBo> spubos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu,spuBo);

            //查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            //查询分类名称
            List<String> strings = this.categoryService.queryNamesByIds(List.of(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(strings,"-"));
            return spuBo;
        }).collect(Collectors.toList());
        //返回一个PageResult<SpuBo>
        return new PageResult<>(info.getTotal(),spubos);
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 新增商品
     * @param spuBo
     */
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        // 新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);

        sendMsg("insert",spuBo.getId());
    }

    private void sendMsg(String type, Long id) {
        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据SPu的ID查询SpuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据SpuId查询sku的集合
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 更新商品信息
     * @param spuBo
     * @return
     */
    public void updateGoods(SpuBo spuBo) {
        //根据spuid查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(record);
        //删除stock
        skus.forEach(sku->this.stockMapper.deleteByPrimaryKey(sku.getId()));
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku);
        //新增sku和新增stock
        this.saveSkuAndStock(spuBo);

        //更新SPU和spudetail
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update",spuBo.getId());
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    public Sku querySkuBySkuId(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
