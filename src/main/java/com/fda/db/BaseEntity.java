package com.fda.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fda.reflect.Reflections;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * DO实体类基类
 * 包含主键id字段、创建时间、最后修改时间、删除时间、删除状态等通用属性
 * Mybatis plus/JPA都可用
 *
 * @author Hulunliang
 * @since 2019/05/21
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity<T extends Model> extends Model {
    /**
     * 用于与UI交互的缺省日期时间呈现格式，yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATETIME_FORMAT_FOR_UI = "yyyy-MM-dd HH:mm:ss";

    /**
     * 用于与UI交互的缺省日期呈现格式，yyyy-MM-dd
     */
    public static final String DEFAULT_DATE_FORMAT_FOR_UI = "yyyy-MM-dd";

    /**
     * 用于与UI交互的缺省时间呈现格式，HH:mm:ss
     */
    public static final String DEFAULT_TIME_FORMAT_FOR_UI = "HH:mm:ss";

    /**
     * 东八区timezone常量GMT+8
     */
    public static final String TIMEZONE_EAST_8 = "GMT+8";

    /**
     * 创建时间，自动在create时获取系统时间赋值
     */
    @JsonFormat(locale = "zh", timezone = TIMEZONE_EAST_8, pattern = DEFAULT_DATETIME_FORMAT_FOR_UI)
    @DateTimeFormat(pattern = DEFAULT_DATETIME_FORMAT_FOR_UI)
    @TableField(value = "gmt_created")
    @Column(name = "gmt_created", columnDefinition = "datetime COMMENT '创建时间'")
    private Date gmtCreated;

    /**
     * 最后修改时间，自动在modify时获取系统时间赋值
     */
    @JsonFormat(locale = "zh", timezone = TIMEZONE_EAST_8, pattern = DEFAULT_DATETIME_FORMAT_FOR_UI)
    @DateTimeFormat(pattern = DEFAULT_DATETIME_FORMAT_FOR_UI)
    @TableField(value = "gmt_modified")
    @Column(name = "gmt_modified", columnDefinition = "datetime COMMENT '最后修改时间'")
    private Date gmtModified;

    /**
     * 删除标记（逻辑删除）1——已删除，0——未删除
     */
    @TableField(value = "delete_flag")
    @Column(name = "delete_flag", columnDefinition = "tinyint default 0 COMMENT '删除标记'")
    @JsonIgnore
    private Integer deleteFlag = 0;

    /**
     * 附加属性，采用Map存放，通过get(key)、set(key,value)方式访问，不存库。用于后台返回一些额外信息
     */
    @Transient
    @TableField(exist = false)
    private Map<String, Object> extraProperties;

    /**
     * 获取主键值
     *
     * @return ID值
     */
    protected Serializable getId() {
        return pkVal();
    }

    public Object get(String key) {
        if (extraProperties == null) {
            return null;
        }
        return extraProperties.get(key);
    }

    public void set(String key, Object value) {
        if (extraProperties == null) {
            extraProperties = new HashMap();
        }
        extraProperties.put(key, value);
    }

    private static Object getFieldDisplayText(Object value) {
        if (value instanceof Date) {
            return new SimpleDateFormat(DEFAULT_DATETIME_FORMAT_FOR_UI).format((Date) value);
        } else {
            return value;
        }
    }

    /**
     * 获取主键属性的值
     * @param entity 实体类
     * @return 主键属性的值
     */
    public static Serializable getIdValue(BaseEntity entity) {
        return (Serializable)Reflections.getFieldValue(entity, getIdPropertyName(entity));
    }

    /**
     * 获取主键属性名
     * @param entity 实体类
     * @return 主键属性名
     */
    public static String getIdPropertyName(BaseEntity entity) {
        return (String)Reflections.getStaticFieldValue(entity.getClass(), "KEY_PROPERTY");
    }
}