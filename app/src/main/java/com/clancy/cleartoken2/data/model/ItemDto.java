package com.clancy.cleartoken2.data.model;

import java.io.Serializable;

import com.clancy.cleartoken2.data.model.Constants.ColorInfo;

public class ItemDto implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String owner;
    private String options;
    private int increments;
    private int incrementsMax;
    private String photoFile;
    private double unitPrice;
    private String deviceAddress;
    private String deviceName;

    private int colorRes;
    private int labelColorRes;
    private ColorInfo colorTheme;

    private int units;
    public static final int UNITS_M = 0;
    public static final int UNITS_H = 1;

    private int type;
    public static final int TYPE_BOX = 0;
    public static final int TYPE_INCAR = 1;
    public static final int TYPE_WEDGE = 2;

    /**
     * @param description
     * @param options
     * @param iconName
     * @param colorRes    - will be set for the label, and the initial state
     */
    public ItemDto(String owner, String options, String photoFile, int colorRes, ColorInfo colorTheme, String deviceAddress, double unitPrice) {
        this.owner = owner;
        this.options = options;
        this.photoFile = photoFile;
        this.colorRes = colorRes;
        this.labelColorRes = colorRes;
        this.colorTheme = colorTheme;
        this.deviceAddress = deviceAddress;
        this.unitPrice = unitPrice;
    }


    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }


    public int getIncrements() {
        return increments;
    }


    public void setIncrements(int increments) {
        this.increments = increments;
    }


    public int getIncrementsMax() {
        return incrementsMax;
    }


    public void setIncrementsMax(int incrementsMax) {
        this.incrementsMax = incrementsMax;
    }


    public String getPhotoFile() {
        return photoFile;
    }


    public void setPhotoFile(String photoFile) {
        this.photoFile = photoFile;
    }


    public int getUnits() {
        return units;
    }


    public void setUnits(int units) {
        this.units = units;
    }


    public int getType() {
        return type;
    }


    public void setType(int type) {
        this.type = type;
    }


    public ItemDto() {
        // TODO Auto-generated constructor stub
    }


    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getLabelColorRes() {
        return labelColorRes;
    }

    public void setLabelColorRes(int labelColorRes) {
        this.labelColorRes = labelColorRes;
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }

    public String getIconName() {
        return photoFile;
    }

    public void setIconName(String iconName) {
        this.photoFile = iconName;
    }

    public String getDescription() {
        return owner;
    }

    public void setDescription(String description) {
        this.owner = description;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public ColorInfo getColorTheme() {
        return colorTheme;
    }

    public void setColorTheme(ColorInfo colorTheme) {
        this.colorTheme = colorTheme;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
