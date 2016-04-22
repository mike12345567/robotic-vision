package com.queens.entities;

import com.queens.utilities.MatOperations;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class FactorBtnListener implements ActionListener {
    boolean isAlpha = false;
    JLabel lbl;

    public FactorBtnListener(boolean isAlpha, JLabel lbl) {
        this.isAlpha = isAlpha;
        this.lbl = lbl;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double factorChange = ((JButton) e.getSource()).getText().equals("-") ?
                MatOperations.factorChange * -1 : MatOperations.factorChange;

        DecimalFormat format = new DecimalFormat("#.#");
        if (isAlpha) {
            MatOperations.alphaFactor += factorChange;
            lbl.setText("Alpha: " + format.format(MatOperations.alphaFactor));
        } else {
            MatOperations.betaFactor += factorChange * 5;
            lbl.setText("Beta: " + format.format(MatOperations.betaFactor));
        }

    }

}
