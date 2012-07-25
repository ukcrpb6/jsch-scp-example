package com.pressassociation;

public class SSHException extends RuntimeException {
    public SSHException() {
    }

    public SSHException(String s) {
        super(s);
    }

    public SSHException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SSHException(Throwable throwable) {
        super(throwable);
    }
}
