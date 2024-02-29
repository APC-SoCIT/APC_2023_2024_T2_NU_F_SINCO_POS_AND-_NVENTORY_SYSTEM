package com.example.sincopossystemfullversion.textparser;

import com.example.sincopossystemfullversion.EscPosPrinter.EscPosPrinterCommands;
import com.example.sincopossystemfullversion.exceptions.EscPosConnectionException;
import com.example.sincopossystemfullversion.exceptions.EscPosEncodingException;

public interface IPrinterTextParserElement {
    int length() throws EscPosEncodingException;
    IPrinterTextParserElement print(EscPosPrinterCommands printerSocket) throws EscPosEncodingException, EscPosConnectionException;
}
