SELECT
pav.spu_id,
ag.attr_group_name,
ag.attr_group_id,
aar.attr_id,
a.attr_name,
pav.attr_value
FROM pms_attr_group ag
LEFT JOIN pms_attr_attrgroup_relation aar ON aar.attr_group_id = ag.attr_group_id
LEFT JOIN pms_attr a ON a.attr_id = aar.attr_id
LEFT JOIN pms_product_attr_value pav ON pav.attr_id = a.attr_id
WHERE ag.catelog_id = 225 AND pav.spu_id = 13;
       SELECT
          ssav.attr_id attr_id,
          ssav.attr_name attr_name,
       GROUP_CONCAT(DISTINCT ssav.attr_value) attr_value
       FROM pms_sku_info si
       LEFT JOIN pms_sku_sale_attr_value ssav ON ssav.sku_id = si.sku_id
       WHERE si.spu_id = 13
       GROUP BY ssav.attr_id, ssav.attr_name;