package ru.glowcase.user;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CaseUser {

    private final UUID uuid;
    private String name;
    private final Map<String, Integer> keys = new ConcurrentHashMap<>();
    private int totalOpened = 0;
    private final Map<String, Integer> caseOpened = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public CaseUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeys(String caseId) {
        if (caseId == null) return 0;
        return keys.getOrDefault(caseId.toLowerCase(), 0);
    }

    public void setKeys(String caseId, int amount) {
        if (caseId == null) return;
        int clamped = Math.max(0, amount);
        keys.put(caseId.toLowerCase(), clamped);
        dirty = true;
    }

    public void addKeys(String caseId, int amount) {
        if (caseId == null || amount <= 0) return;
        setKeys(caseId, getKeys(caseId) + amount);
    }

    public boolean takeKey(String caseId) {
        return takeKeys(caseId, 1);
    }

    public boolean takeKeys(String caseId, int amount) {
        if (caseId == null || amount <= 0) return false;
        int current = getKeys(caseId);
        if (current >= amount) {
            setKeys(caseId, current - amount);
            return true;
        }
        return false;
    }

    public int getTotalOpened() {
        return totalOpened;
    }

    public void setTotalOpened(int totalOpened) {
        this.totalOpened = Math.max(0, totalOpened);
        dirty = true;
    }

    public void incrementOpened(String caseId) {
        this.totalOpened++;
        if (caseId != null) {
            caseOpened.merge(caseId.toLowerCase(), 1, Integer::sum);
        }
        dirty = true;
    }

    public int getCaseOpened(String caseId) {
        if (caseId == null) return 0;
        return caseOpened.getOrDefault(caseId.toLowerCase(), 0);
    }

    public void setCaseOpened(String caseId, int count) {
        if (caseId == null) return;
        caseOpened.put(caseId.toLowerCase(), Math.max(0, count));
        dirty = true;
    }

    public Map<String, Integer> getAllKeys() {
        return Collections.unmodifiableMap(keys);
    }

    public Map<String, Integer> getAllCaseOpened() {
        return Collections.unmodifiableMap(caseOpened);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
