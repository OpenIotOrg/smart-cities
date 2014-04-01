/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.core;

/**
 *
 * @author admin-jacoby
 */
public enum FieldType {

    Int,
    Double,
    String;

    public String toString() {
        String result = "";
        switch (this) {
            case Int:
                result = "int";
                break;
            case Double:
                result = "double";
                break;
            case String:
                result = "string";
                break;
            default:

        }
        return result;
    }
}
