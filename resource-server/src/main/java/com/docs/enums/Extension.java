package com.docs.enums;

public enum Extension {
    DOCX;

    public String extensionWithDot(){
        return "." + this.name();
    }
}
