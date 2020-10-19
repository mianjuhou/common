package com.fda.db;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 日期类属性查询条件扩展
 *
 * @author Hulunliang
 * @since 2020/3/28.
 */
@Data
public class DateQueryCondition {
    /**
     * 开始时间
     */
    @JsonFormat(locale = "zh", timezone = BaseEntity.TIMEZONE_EAST_8, pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    @DateTimeFormat(pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(locale = "zh", timezone = BaseEntity.TIMEZONE_EAST_8, pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    @DateTimeFormat(pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    private Date endTime;

    /**
     * 当前时间
     */
    @JsonFormat(locale = "zh", timezone = BaseEntity.TIMEZONE_EAST_8, pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    @DateTimeFormat(pattern = BaseEntity.DEFAULT_DATETIME_FORMAT_FOR_UI)
    private Date currentTime;

    /**
     * 是否查询时间范围
     * @return
     */
    @JsonIgnore
    public boolean isQueryTimeScope() {
        return (currentTime == null) ? true : false;
    }

    /**
     * 是否不限时间
     * @return
     */
    @JsonIgnore
    public boolean isTimeNotLimit() {
        return (currentTime == null && startTime == null && endTime == null) ? true : false;
    }
}
