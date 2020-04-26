package com.qiangzengy.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 第一个范型 指定的是注解（@ListValue）
 * 第二个范型 校验的类型（Integer类型）
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set=new HashSet<>();


    /**
     * 判断value是否满足 int[] 里面的值
     * @param value
     * @param context
     * @return
     */
    //判断是否校验成功
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        //set是否包含value的值
        return set.contains(value);
    }

    //初始化方法，将ListValue注解的详细信息给我们，即（@ListValue(vals = {0,1})这个信息）
    @Override
    public void initialize(ListValue constraintAnnotation) {

        int [] valus =constraintAnnotation.vals();
        for (int val:valus){
            set.add(val);
        }
    }
}
