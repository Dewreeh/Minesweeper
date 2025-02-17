package org.repin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class ErrorResponse extends Throwable {
    private String error;
    public ErrorResponse(String error){
        this.error = error;
    }


}
