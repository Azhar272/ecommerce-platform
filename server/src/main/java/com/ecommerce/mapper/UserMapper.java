package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * 继承 BaseMapper 后即可获得基本的 CRUD 方法，无需写 XML
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
