package com.espressif.ui.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class GlowsignDeviceViewModel extends AndroidViewModel {
    private final GlowsignDeviceRepository repository;
    private final LiveData<List<GlowsignDevice>> allDevices;
    public GlowsignDeviceViewModel(@NonNull Application application) {
        super(application);
        repository = new GlowsignDeviceRepository(application);
        allDevices = repository.getAllDevices();
    }

    public void insert(GlowsignDevice model) {
        repository.insert(model);
    }

    public void update(GlowsignDevice model) {
        repository.update(model);
    }

    public void delete(GlowsignDevice model) {
        repository.delete(model);
    }

    // below method is to get all the courses in our list.
    public LiveData<List<GlowsignDevice>> getAllCourses() {
        return allDevices;
    }
}
