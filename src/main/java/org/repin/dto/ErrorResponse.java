package org.repin.dto;

public class ErrorResponse extends Throwable {
    private String error;
    public ErrorResponse(String error){
        this.error = error;
    }

    public String getError(){
        return this.error;
    }
}
