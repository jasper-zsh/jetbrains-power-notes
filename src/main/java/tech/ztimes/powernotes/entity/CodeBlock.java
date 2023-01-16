package tech.ztimes.powernotes.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeBlock {
    private Integer lineNumber;
    private Integer endLineNumber;
}
