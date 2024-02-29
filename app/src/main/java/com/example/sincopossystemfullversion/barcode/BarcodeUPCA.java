package com.example.sincopossystemfullversion.barcode;

import com.example.sincopossystemfullversion.EscPosPrinter.EscPosPrinterCommands;
import com.example.sincopossystemfullversion.EscPosPrinter.EscPosPrinterSize;
import com.example.sincopossystemfullversion.exceptions.EscPosBarcodeException;

public class BarcodeUPCA extends BarcodeNumber {

    public BarcodeUPCA(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_UPCA, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return 12;
    }
}
