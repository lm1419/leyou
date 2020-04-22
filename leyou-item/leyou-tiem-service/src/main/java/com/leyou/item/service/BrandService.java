package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    BrandMapper brandMapper;

    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Brand.class);
        //根据name模糊查询，或者根据首字母查询
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);

        //进行分页的设置
        PageHelper.startPage(page, rows);

        //进行排序的设置
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        List<Brand> items = brandMapper.selectByExample(example);
        //包装成PageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(items);
        return new PageResult<Brand>(pageInfo.getTotal(), pageInfo.getList());
    }


    /**
     * 新增品牌
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {

        //先新增brand
        this.brandMapper.insertSelective(brand);

        //新增中间表
        cids.forEach(cid -> {
            brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }

    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectBrandsByCid(cid);

    }

    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }


}
