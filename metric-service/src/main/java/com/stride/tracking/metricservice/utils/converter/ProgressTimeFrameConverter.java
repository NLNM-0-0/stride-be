package com.stride.tracking.metricservice.utils.converter;

import com.stride.tracking.metric.dto.progress.ProgressTimeFrame;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProgressTimeFrameConverter implements Converter<String, ProgressTimeFrame> {
    @Override
    public ProgressTimeFrame convert(@NonNull String source) {
        for (ProgressTimeFrame tf : ProgressTimeFrame.values()) {
            if (tf.getName().equalsIgnoreCase(source)) {
                return tf;
            }
        }
        throw new IllegalArgumentException("Unknown time frame: " + source);
    }
}
