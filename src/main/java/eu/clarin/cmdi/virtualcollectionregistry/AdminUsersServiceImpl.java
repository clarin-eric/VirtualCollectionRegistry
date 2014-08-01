package eu.clarin.cmdi.virtualcollectionregistry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@Component
public class AdminUsersServiceImpl implements AdminUsersService {

    private final static Logger logger = LoggerFactory.getLogger(AdminUsersServiceImpl.class);
    private final Set<String> adminUsers = new HashSet<>();

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.admindb:}")
    private String adminDb;

    @Override
    public final boolean isAdmin(String user) {
        logger.debug("Checking admin rights of {}", user);
        return adminUsers.contains(user);
    }

    @PostConstruct
    protected final void init() {
        if (adminDb != null && !adminDb.isEmpty()) {
            logger.info("Reading admin user database");
            try {
                loadAdminDatabase(adminDb);
            } catch (IOException e) {
                throw new RuntimeException("Could not load admin user database", e);
            }
        }
        if (adminUsers.isEmpty()) {
            logger.warn("No admin users have been defined");
        } else {
            logger.debug("Admin users: {}", adminUsers);
        }
    }

    private void loadAdminDatabase(String filename) throws IOException {
        adminUsers.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                adminUsers.add(line);
            } // while
        }
    }

}