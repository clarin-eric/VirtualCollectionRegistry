package eu.clarin.cmdi.virtualcollectionregistry.core;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class AdminUsersServiceImplTest {

    private final String path;
    private final AdminUsersService service;

    public AdminUsersServiceImplTest() {
        File f = new File(AdminUsersServiceImplTest.class.getClassLoader().getResource("test-admin-db.conf").getPath());
        path = f.getParent();
        service = new AdminUsersServiceImpl(path, "test-admin-db.conf");
    }

    @Test(expected = RuntimeException.class)
    public void testInitialization() {
        new AdminUsersServiceImpl(path, "test-admin-db-noexisting.conf");
    }

    @Test
    public void testIsAdmin() {
        Assert.assertEquals(true, service.isAdmin("adminuser1"));
        Assert.assertEquals(false, service.isAdmin("adminuser2"));
        Assert.assertEquals(false, service.isAdmin("regularuser"));
    }

    @Test
    public void testGetAdminUsers() {
        Assert.assertEquals(1, service.getAdminUsers().size());
    }
}
