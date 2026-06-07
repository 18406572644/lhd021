package com.community.idle;

import com.community.idle.common.PermissionContext;
import com.community.idle.common.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

    @BeforeEach
    void setUp() {
        PermissionContext.clear();
        UserContext.remove();
    }

    @AfterEach
    void tearDown() {
        PermissionContext.clear();
        UserContext.remove();
    }

    @Test
    void testUserContext() {
        Set<String> roleCodes = new HashSet<>(Collections.singletonList("SUPER_ADMIN"));
        UserContext.CurrentUser user = new UserContext.CurrentUser(1L, "admin", null, roleCodes);
        UserContext.set(user);

        assertEquals(1L, UserContext.getUserId());
        assertEquals("admin", UserContext.getUsername());
        assertTrue(UserContext.hasRole("SUPER_ADMIN"));
        assertTrue(UserContext.isAdmin());
    }

    @Test
    void testPermissionContext() {
        Set<String> permissions = new HashSet<>(Arrays.asList("user:view", "user:add", "user:edit"));
        Set<String> roles = new HashSet<>(Collections.singletonList("OPERATOR"));
        Set<String> apiPermissions = new HashSet<>(Arrays.asList("api:user:list", "api:user:create"));

        PermissionContext.setPermissions(permissions);
        PermissionContext.setRoles(roles);
        PermissionContext.setApiPermissions(apiPermissions);
        PermissionContext.setDataScopeType(5);

        Map<String, Set<Long>> dataScopeMap = new HashMap<>();
        dataScopeMap.put("PICKUP_POINT", new HashSet<>(Arrays.asList(1L, 2L, 3L)));
        PermissionContext.setDataScope(dataScopeMap);

        assertTrue(PermissionContext.hasPermission("user:view"));
        assertTrue(PermissionContext.hasPermission("api:user:list"));
        assertFalse(PermissionContext.hasPermission("user:delete"));

        assertTrue(PermissionContext.hasAnyPermission("user:view", "user:delete"));
        assertFalse(PermissionContext.hasAnyPermission("user:delete", "user:export"));

        assertTrue(PermissionContext.hasRole("OPERATOR"));
        assertFalse(PermissionContext.hasRole("SUPER_ADMIN"));

        assertEquals(5, PermissionContext.getDataScopeType());
        Set<Long> pickupPointIds = PermissionContext.getDataScopeIds("PICKUP_POINT");
        assertNotNull(pickupPointIds);
        assertEquals(3, pickupPointIds.size());
        assertTrue(pickupPointIds.contains(1L));
    }

    @Test
    void testSuperAdminBypass() {
        Set<String> roleCodes = new HashSet<>(Collections.singletonList("SUPER_ADMIN"));
        UserContext.CurrentUser user = new UserContext.CurrentUser(1L, "admin", null, roleCodes);
        UserContext.set(user);

        Set<String> roles = new HashSet<>(Collections.singletonList("SUPER_ADMIN"));
        PermissionContext.setRoles(roles);

        assertTrue(PermissionContext.hasPermission("any:permission"));
        assertTrue(PermissionContext.hasRole("any_role"));
        assertTrue(UserContext.isAdmin());
    }

    @Test
    void testDataScopeType4() {
        PermissionContext.setDataScopeType(4);
        Set<String> roleCodes = new HashSet<>();
        UserContext.CurrentUser user = new UserContext.CurrentUser(100L, "test", null, roleCodes);
        UserContext.set(user);

        assertEquals(4, PermissionContext.getDataScopeType());
        assertEquals(100L, UserContext.getUserId());
    }

    @Test
    void testClearContext() {
        PermissionContext.setPermissions(new HashSet<>(Collections.singleton("test:permission")));
        PermissionContext.setRoles(new HashSet<>(Collections.singleton("TEST_ROLE")));
        
        Set<String> roleCodes = new HashSet<>();
        UserContext.CurrentUser user = new UserContext.CurrentUser(1L, "test", null, roleCodes);
        UserContext.set(user);

        PermissionContext.clear();
        UserContext.remove();

        assertFalse(PermissionContext.hasPermission("test:permission"));
        assertNull(UserContext.getUserId());
    }

    @Test
    void testMultipleRoles() {
        Set<String> roles = new HashSet<>(Arrays.asList("OPERATOR", "AUDITOR"));
        PermissionContext.setRoles(roles);

        assertTrue(PermissionContext.hasRole("OPERATOR"));
        assertTrue(PermissionContext.hasRole("AUDITOR"));
        assertTrue(PermissionContext.hasAnyRole("OPERATOR", "OTHER"));
        assertFalse(PermissionContext.hasAnyRole("SUPER_ADMIN", "OTHER"));
    }

    @Test
    void testEmptyDataScope() {
        PermissionContext.setDataScopeType(5);
        Set<Long> emptyIds = PermissionContext.getDataScopeIds("NON_EXISTENT");
        assertNotNull(emptyIds);
        assertTrue(emptyIds.isEmpty());
    }

    @Test
    void testHasAllPermission() {
        Set<String> permissions = new HashSet<>(Arrays.asList("user:view", "user:add", "user:edit"));
        PermissionContext.setPermissions(permissions);

        assertTrue(PermissionContext.hasAllPermission("user:view", "user:add"));
        assertFalse(PermissionContext.hasAllPermission("user:view", "user:delete"));
    }

    @Test
    void testDefaultDataScopeType() {
        assertEquals(4, PermissionContext.getDataScopeType());
        PermissionContext.clear();
    }

    @Test
    void testEmptyPermissions() {
        PermissionContext.setPermissions(Collections.emptySet());
        assertFalse(PermissionContext.hasPermission("any"));
        assertFalse(PermissionContext.hasAnyPermission("any", "any2"));
        assertFalse(PermissionContext.hasAllPermission("any"));
    }
}
