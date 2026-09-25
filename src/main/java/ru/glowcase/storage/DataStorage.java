package ru.glowcase.storage;

import ru.glowcase.user.CaseUser;
import java.util.UUID;

public interface DataStorage {

    void init() throws Exception;

    CaseUser loadUser(UUID uuid, String name);

    void saveUser(CaseUser user);

    void saveAll();

    void close();
}
