package com.boot.plugin.mapper;

import com.boot.plugin.annotations.EnableTableScan;
import com.boot.plugin.bean.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author panlf
 * @date 2020/12/25
 */
@Mapper
@EnableTableScan(enable = true)
public interface OrderMapper {
    @Select("select * from c_order where id = #{id}")
    Order findById(@Param("id") long id);

    @Select("select * from c_order")
    List<Order> selectAll();
}
