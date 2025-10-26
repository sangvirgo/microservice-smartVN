package com.smartvn.admin_service.config;

import com.smartvn.admin_service.enums.UserRole;
import com.smartvn.admin_service.exceptions.AppException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class StringToUserRoleConverter implements Converter<String, UserRole> {
    @Override
    public UserRole convert(String source) {
        try {
            return UserRole.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid role: " + source, HttpStatus.BAD_REQUEST);
        }
    }
}