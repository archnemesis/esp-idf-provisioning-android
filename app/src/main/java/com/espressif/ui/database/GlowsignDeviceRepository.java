package com.espressif.ui.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class GlowsignDeviceRepository {
    private GlowsignDeviceDao dao;
    private LiveData<List<GlowsignDevice>> allDevices;

    public GlowsignDeviceRepository(Application application) {
        dao = GlowsignDatabase.getInstance(application).glowsignDeviceDao();
        allDevices = dao.getAll();
    }

    public void insert(GlowsignDevice device) {
        new InsertDeviceAsyncTask(dao).execute(device);
    }

    public void update(GlowsignDevice device) {
        new UpdateDeviceAsyncTask(dao).execute(device);
    }

    public void delete(GlowsignDevice device) {
        new DeleteDeviceAsyncTask(dao).execute(device);
    }

    public LiveData<List<GlowsignDevice>> getAllDevices() {
        return allDevices;
    }

    private static class InsertDeviceAsyncTask extends AsyncTask<GlowsignDevice, Void, Void> {
        private GlowsignDeviceDao dao;

        private InsertDeviceAsyncTask(GlowsignDeviceDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(GlowsignDevice... device) {
            dao.insert(device[0]);
            return null;
        }
    };

    private static class UpdateDeviceAsyncTask extends AsyncTask<GlowsignDevice, Void, Void> {
        private GlowsignDeviceDao dao;

        private UpdateDeviceAsyncTask(GlowsignDeviceDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(GlowsignDevice... device) {
            dao.update(device[0]);
            return null;
        }
    };

    private static class DeleteDeviceAsyncTask extends AsyncTask<GlowsignDevice, Void, Void> {
        private GlowsignDeviceDao dao;

        private DeleteDeviceAsyncTask(GlowsignDeviceDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(GlowsignDevice... device) {
            dao.delete(device[0]);
            return null;
        }
    };
}
