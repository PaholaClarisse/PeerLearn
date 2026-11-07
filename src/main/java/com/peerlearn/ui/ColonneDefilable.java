package com.peerlearn.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/** Panneau vertical qui épouse la largeur de son JScrollPane (pas de barre horizontale). */
class ColonneDefilable extends JPanel implements Scrollable {

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 96; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
