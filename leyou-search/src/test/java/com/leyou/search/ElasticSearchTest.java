package com.leyou.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.respository.GoodsRespository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    GoodsClient goodsClient;

    @Autowired
    SearchService searchService;

    @Autowired
    GoodsRespository goodsRespository;
    @Test
    public void test02() {
        elasticsearchTemplate.deleteIndex(Goods.class);
    }

    @Test
    public void test01() {
        Integer page = 1;
        Integer rows = 100;
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);

        do {
            // 分页查询spu，获取分页结果集。
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, null, page, rows);
            // 获取当前页的数据
            List<SpuBo> items = result.getItems();
            // 处理List<SpuBo> =>List<Goods>
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            //执行新增数据的方法
            this.goodsRespository.saveAll(goodsList);
            rows = items.size();
            page++;
        } while (rows == 100);


    }
}
