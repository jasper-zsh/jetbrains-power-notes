package tech.ztimes.powernotes.remote;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event<T> {
    private String event;
    private T payload;
}
