package com.example.sincopossystemfullversion.barcode;

import com.example.sincopossystemfullversion.EscPosPrinter.EscPosPrinterCommands;
import com.example.sincopossystemfullversion.EscPosPrinter.EscPosPrinterSize;
import com.example.sincopossystemfullversion.exceptions.EscPosBarcodeException;

public class Barcode128 extends Barcode {
    public Barcode128(EscPosPrinterSize printerSize, String code, float widthMM, float heightMM, int textPosition) throws EscPosBarcodeException {
        super(printerSize, EscPosPrinterCommands.BARCODE_TYPE_128, code, widthMM, heightMM, textPosition);
    }

    @Override
    public int getCodeLength() {
        return this.code.length();
    }

    @Override
    public int getColsCount() {
        return (this.getCodeLength() + 5) * 11;
    }
}
