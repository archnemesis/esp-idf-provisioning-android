package com.espressif.ui.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GlowsignDevice {
    @PrimaryKey
    @NonNull
    public String name;

    @ColumnInfo(name = "model")
    public String model;
}
