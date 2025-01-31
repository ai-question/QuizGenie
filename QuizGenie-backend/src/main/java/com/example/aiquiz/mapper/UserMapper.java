package com.example.aiquiz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.example.aiquiz.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);
    
    @Insert("INSERT INTO user (username, password, email, create_time, update_time) " +
           "VALUES (#{username}, #{password}, #{email}, #{createTime}, #{updateTime})")
    void insert(User user);

    @Update("UPDATE user SET avatar = #{avatar} WHERE id = #{userId}")
    void updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);
} 