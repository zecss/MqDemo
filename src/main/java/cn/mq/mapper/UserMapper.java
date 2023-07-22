package cn.mq.mapper;

import cn.mq.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: TODO
 * @Param
 * @return
 */
@Mapper
public interface UserMapper {
    int reg(User user);
}
