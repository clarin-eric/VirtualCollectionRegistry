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
                throw new RuntimeException("Could not load admin user database. Dir"+adminDbBaseDir+", file="+adminDb, e);
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
     /*
        if(adminDbBaseDir == null || adminDbBaseDir.isEmpty()) {
            adminDbBaseDir = System.getProperty("user.home");
            logger.debug("eu.clarin.cmdi.virtualcollectionregistry.admindb.basedir not set, using home directory: "+adminDbBaseDir);
        }
       */
        /*
        String filenameWithPath = filename;
        if(adminDbBaseDir.endsWith("/") && filename.startsWith("/")) {
            filenameWithPath = adminDbBaseDir + filename.substring(1);
        } else if(!adminDbBaseDir.endsWith("/") && !filename.startsWith("/")) {
            filenameWithPath = adminDbBaseDir + "/" + filename;
        } else {
            filenameWithPath = adminDbBaseDir + filename;
        }
        */
        Path filenameWithPath = Paths.get(adminDbBaseDir, filename);

        logger.info("filenameWithPath: "+filenameWithPath.toAbsolutePath().toString());
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filenameWithPath.toAbsolutePath().toString())))) {
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


    public List<String> getAdminUsers() {
        List<String> admins = new LinkedList<>();
        for(String admin : this.adminUsers) {
            admins.add(admin);
        }
        Collections.sort(admins);
        return admins;
    }

}
