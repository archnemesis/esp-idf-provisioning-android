package com.espressif.ui.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GlowsignDeviceDao {
    @Query("SELECT * FROM GlowsignDevice")
    public LiveData<List<GlowsignDevice>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(GlowsignDevice device);

    @Update
    public void update(GlowsignDevice device);

    @Delete
    public void delete(GlowsignDevice device);

}
