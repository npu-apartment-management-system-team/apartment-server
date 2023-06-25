package edu.npu.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author : [wangminan]
 * @description : [自定义异常]
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApartmentException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 165421815L;

    private  String msg;

    public ApartmentException() {
        super();
    }

    public ApartmentException(String message) {
        super(message);
        this.msg = message;
    }

    public ApartmentException(ApartmentError commonError, String message) {
        super(commonError.getErrMessage() + " " + message);
        this.msg = commonError.getErrMessage() + " " + message;
    }
}
