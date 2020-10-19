package com.fda.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Hulunliang
 * @since 2020/4/3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifyResult<T extends BaseEntity> {
    private T oldData;
    private T newData;
}
