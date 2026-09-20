package com.falcon.car.obd;

import java.io.IOException;

/** Raised when the adapter answers but the answer is not usable. */
public class ObdException extends IOException {

    public ObdException(String message) {
        super(message);
    }
}
