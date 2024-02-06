package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.List;

/**
 *
 * @author twagoo
 */
public interface AdminUsersService {

    boolean isAdmin(String user);
    public List<String> getAdminUsers();
}
