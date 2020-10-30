/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package models.flow;

public enum PromptType {
    NONE('X', "None"),
    TOAST('T', "Toast"),
    DIALOG('D', "Dialog"),
    CONFIRM('C', "Confirm"),
    CONFIRM_NEW('N', "Confirm Top"),
    SUB_FLOW_DIVIDER('S', "Sub Flow Divider");

    PromptType(char code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final char code;
    private final String desc;

    public short getCode() {
        return (short) code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCodeString() {
        return String.valueOf(code);
    }

    public static PromptType from(short code) {
        for (PromptType prompt : values()) {
            if (prompt.getCode() == code) {
                return prompt;
            }
        }
        return NONE;
    }

    public static PromptType from(String code) {
        if (code == null) {
            return NONE;
        }
        return from((short) code.charAt(0));
    }

}
