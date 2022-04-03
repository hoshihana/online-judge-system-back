package pers.wjx.ojsb.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JudgeRequest {
    private String sourceCode;

    private Integer languageId;

    private String compilerOptions;

    private String commandLineArguments;

    private String stdin;

    private String expectedOutput;

    private Float cpuTimeLimit;

    private Float cpuExtraTime;

    private Float wallTimeLimit;

    private Float memoryLimit;

    private Float stackLimit;

    public void setSourceCode(String sourceCode) {
        if(sourceCode == null) {
            this.sourceCode = null;
        } else {
            this.sourceCode = Base64Utils.encodeToString(sourceCode.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void setStdin(String stdin) {
        if(stdin == null) {
            this.stdin = null;
        } else {
            this.stdin = Base64Utils.encodeToString(stdin.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void setExpectedOutput(String expectedOutput) {
        if(expectedOutput == null) {
            this.expectedOutput = null;
        } else {
            this.expectedOutput = Base64Utils.encodeToString(expectedOutput.getBytes(StandardCharsets.UTF_8));
        }
    }
}
