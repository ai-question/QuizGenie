package com.example.aiquiz.mapper;

import com.example.aiquiz.model.InvitationCode;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;

@Mapper
public interface InvitationCodeMapper {
    
    @Select("SELECT * FROM invitation_code WHERE code = #{code}")
    InvitationCode findByCode(String code);
    
    @Update("UPDATE invitation_code SET used = true, used_by = #{username}, used_time = #{usedTime} WHERE code = #{code}")
    int markAsUsed(@Param("code") String code, @Param("username") String username, @Param("usedTime") LocalDateTime usedTime);
} 