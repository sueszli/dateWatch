package at.ac.tuwien.sepm.groupphase.backend.common.sse;


import com.fasterxml.jackson.annotation.JsonIgnore;

public interface SseDto {

    @JsonIgnore
    String getEventType();
}
