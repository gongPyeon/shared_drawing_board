package distributed.cm.server.repository;


import distributed.cm.common.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class UserRepository {
    private static final ConcurrentHashMap<String, User> sessionStore = new ConcurrentHashMap();

    public void saveUser(String sessionId, User user) {
        sessionStore.put(sessionId, user);
        log.info("Save user : sessionId={}, userId={}", sessionId, user.getUserId());
    }

    public void removeUser(String sessionId){
        sessionStore.remove(sessionId);
    }
    public User findUserBySessionId(String sessionId){
        return sessionStore.get(sessionId);
    }
}
