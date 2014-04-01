/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openiot.gsndatapusher.gui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author admin-jacoby
 */
public class ReflectionTableModel extends AbstractTableModel {

    private Object object;
    private static String[] columns = {"name", "value"};
    private List<String> names = new ArrayList<String>();
    private List<Object> values = new ArrayList<Object>();
    private boolean onlyWriteableFields = true;

    public ReflectionTableModel(Object object, boolean onlyWritableFields) {      
        this.object = object;
        this.onlyWriteableFields = onlyWritableFields;
        getData();
    }

    public ReflectionTableModel(Object object) {
        this(object, true);
    }

    @Override
    public String getColumnName(int column) {
        if (column < 0 || column >= columns.length) {
            return null;
        }
        return columns[column];
    }

    private void getData() {
        try {
            BeanInfo info = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) {
                if (onlyWriteableFields && pd.getWriteMethod() == null) {
                    continue;
                }
                Object value = pd.getReadMethod().invoke(object);
                names.add(pd.getDisplayName());
                values.add(value);
            }

        } catch (IntrospectionException ex) {
            Logger.getLogger(ReflectionTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ReflectionTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ReflectionTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ReflectionTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getRowCount() {
        return names.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            return null;
        }
        if (columnIndex < 0 || columnIndex >= getColumnCount()) {
            return null;
        }
        if (columnIndex == 0) {
            return names.get(rowIndex);
        } else if (columnIndex == 1) {
            return values.get(rowIndex);
        } else {
            return null;
        }
    }

}
