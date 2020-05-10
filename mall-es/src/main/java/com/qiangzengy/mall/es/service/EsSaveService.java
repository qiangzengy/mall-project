package com.qiangzengy.mall.es.service;

import com.qiangzengy.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface EsSaveService {


    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
