package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper GroupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    /**
     * 根据分类id查询参数组
     *
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return this.GroupMapper.select(record);
    }

    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean search) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(search);
        return this.paramMapper.select(record);
    }

    public List<SpecGroup> queryGroupsWithParam(Long cid) {
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(specGroup -> {
            List<SpecParam> specParams = this.queryParams(specGroup.getId(), null, null, null);
            specGroup.setParams(specParams);
        });
        return groups;
    }
}
