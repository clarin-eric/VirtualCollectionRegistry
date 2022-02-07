package eu.clarin.cmdi.virtualcollectionregistry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.admindb:vcr-admin.conf}")
    private String adminDb;

    @Value("${eu.clarin.cmdi.virtualcollectionregistry.admindb.basedir:.}")
    private String adminDbBaseDir;

    public AdminUsersServiceImpl() {}

    public AdminUsersServiceImpl(String adminDbBaseDir, String adminDb) {
        this.adminDbBaseDir = adminDbBaseDir;
        this.adminDb = adminDb;
        init();
    }

    @Override
    public final boolean isAdmin(String user) {
        logger.debug("Checking admin rights of {}", user);
        return adminUsers.contains(user);
    }

    @PostConstruct
    protected final void init() {
        logger.info("adminDbBaseDir={}, adminDb={}", adminDbBaseDir, adminDb);
        if (adminDb != null && !adminDb.isEmpty()) {
            logger.info("Reading admin user database");
            try {
                Path filenameWithPath = Paths.get(adminDbBaseDir, adminDb);
                String absolutePath = filenameWithPath.toAbsolutePath().toString();
                logger.info("filenameWithPath: "+absolutePath);

                loadAdminDatabase(absolutePath);
            } catch (IOException e) {
                throw new RuntimeException("Could not load admin user database. Dir"+adminDbBaseDir+", file="+adminDb, e);
            }
        }
        if (adminUsers.isEmpty()) {
            logger.warn("No admin users have been defined");
        } else {
            logger.debug("Admin users: {}", adminUsers);
        }
    }

    /**
     * Load file from disk and read each line as a single admin user. Lines starting with a hash (#) are skipped as
     * comments.
     *
     * @param absolute_path_to_filename
     * @throws IOException
     */
    private void loadAdminDatabase(String absolute_path_to_filename) throws IOException {
        adminUsers.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolute_path_to_filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                adminUsers.add(line);
            }
        }
    }

    public List<String> getAdminUsers() {
        List<String> admins = new LinkedList<>();
        for(String admin : this.adminUsers) {
            admins.add(admin);
        }
        Collections.sort(admins);
        return admins;
    }
}
