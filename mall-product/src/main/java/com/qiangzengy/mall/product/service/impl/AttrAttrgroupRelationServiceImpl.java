package com.qiangzengy.mall.product.service.impl;

        import com.qiangzengy.common.utils.Query;
        import org.springframework.stereotype.Service;

        import java.util.Map;

        import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
        import com.baomidou.mybatisplus.core.metadata.IPage;
        import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
        import com.qiangzengy.common.utils.PageUtils;

        import com.qiangzengy.mall.product.dao.AttrAttrgroupRelationDao;
        import com.qiangzengy.mall.product.entity.AttrAttrgroupRelationEntity;
        import com.qiangzengy.mall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }
}