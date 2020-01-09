package de.tudarmstadt.informatik.hostage.logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SyncData implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<NetworkRecord> networkRecords;
    public ArrayList<SyncRecord> syncRecords;

    public SyncData(){
        this.networkRecords = new ArrayList<NetworkRecord>();
        this.syncRecords = new ArrayList<SyncRecord>();
    }
}
