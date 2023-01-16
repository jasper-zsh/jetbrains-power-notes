package tech.ztimes.powernotes.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class FlowNoteRelation {
    @JSONField(name = "flow_id")
    private Long flowId;
    @JSONField(name = "note_id")
    private Long noteId;
    private Integer position;
}
