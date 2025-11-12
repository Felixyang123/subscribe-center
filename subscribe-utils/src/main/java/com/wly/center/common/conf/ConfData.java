package com.wly.center.common.conf;

import com.wly.center.core.exception.BusinessException;
import com.wly.center.core.exception.BusinessExceptions;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class ConfData {

    private final Class<?> type;

    private final String dataStr;

    private final Object data;

    public ConfData(Class<?> type, String dataStr) {
        this.type = type;
        this.dataStr = dataStr;

        if (!StringUtils.hasText(dataStr)) {
            this.data = null;
        } else if (String.class.isAssignableFrom(type)) {
            this.data = dataStr;
        } else if (Integer.class.isAssignableFrom(type)) {
            this.data = Integer.parseInt(dataStr);
        } else if (Long.class.isAssignableFrom(type)) {
            this.data = Long.parseLong(dataStr);
        } else if (Boolean.class.isAssignableFrom(type)) {
            this.data = Boolean.parseBoolean(dataStr);
        } else if (Double.class.isAssignableFrom(type)) {
            this.data = Double.parseDouble(dataStr);
        } else if (Float.class.isAssignableFrom(type)) {
            this.data = Float.parseFloat(dataStr);
        } else {
            throw new BusinessException(BusinessExceptions.DEFAULT_ERROR.name(), "not support conf type: " + type);
        }

    }

}
