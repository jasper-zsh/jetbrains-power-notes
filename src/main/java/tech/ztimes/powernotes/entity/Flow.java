package tech.ztimes.powernotes.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class Flow {
    private Long id;
    @JSONField(name = "project_name")
    private String projectName;
    private String name;
}
