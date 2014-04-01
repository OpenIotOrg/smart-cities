/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.openiot.gsndatapusher.core.SensorStatus;

/**
 *
 * @author admin-jacoby
 */
public class StateCellRenderer extends JPanel implements TableCellRenderer {

    private JPanel pnlState;
    private JLabel lblStatus;

    public StateCellRenderer() {
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());
        pnlState = new JPanel();
        pnlState.setOpaque(true);
        pnlState.setPreferredSize(new Dimension(20, 20));
        this.add(pnlState, BorderLayout.LINE_START);

        lblStatus = new JLabel();
        this.add(lblStatus, BorderLayout.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        SensorStatus status = (SensorStatus) value;
        if (status != null) {
            lblStatus.setText(status.getMessage());
            pnlState.setBackground(status.getState().toColor());
        }
        return this;
    }

}
