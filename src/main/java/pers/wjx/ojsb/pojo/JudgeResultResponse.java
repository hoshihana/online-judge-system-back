package pers.wjx.ojsb.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.util.Base64Utils;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JudgeResultResponse {

    private String stdout;

    private String stderr;

    private String compileOutput;

    private String message;

    private Integer exitCode;

    private Integer exitSignal;

    private JudgeStatus status;

    private Date createdAt;

    private Date finishedAt;

    private String token;

    private Float time;     // s

    private Float wallTime; // s

    private Float memory;   // KB

    public void setStdout(String stdout) {
        if (stdout == null) {
            this.stdout = null;
        } else {
            this.stdout = new String(Base64Utils.decodeFromString(stdout.replaceAll("\n", "")));
        }
    }

    public void setStderr(String stderr) {
        if(stderr == null) {
            this.stderr = null;
        } else {
            this.stderr = new String(Base64Utils.decodeFromString(stderr.replaceAll("\n", "")));
        }
    }

    public void setCompileOutput(String compileOutput) {
        if(compileOutput == null) {
            this.compileOutput = null;
        } else {
            this.compileOutput = new String(Base64Utils.decodeFromString(compileOutput.replaceAll("\n", "")));
        }
    }

    public void setMessage(String message) {
        if(message == null) {
            this.message = null;
        } else {
            this.message = new String(Base64Utils.decodeFromString(message.replaceAll("\n", "")));
        }
    }
}
