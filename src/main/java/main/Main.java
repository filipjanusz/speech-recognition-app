package main;

import userinterface.UIMain;

import java.awt.*;

public class Main {

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            public void run() {

                UIMain main = new UIMain();
                main.setVisible(true);

            }
        });
    }
}
