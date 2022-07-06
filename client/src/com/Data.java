package com;

import java.util.concurrent.Semaphore;

import org.json.JSONObject;

public class Data {

    private JSONObject jsonObj ;

    final Semaphore mutex = new Semaphore(1);
    final Semaphore empty = new Semaphore(1);
    final Semaphore full = new Semaphore(0);

    Data() {
        this.jsonObj = new JSONObject();
    }
    
    public void write(JSONObject jsonObj) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        this.jsonObj = jsonObj;
        mutex.release();
        full.release();
    }

    public JSONObject read() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        JSONObject j = this.jsonObj;
        mutex.release();
        empty.release();
        return j;
    }

}
