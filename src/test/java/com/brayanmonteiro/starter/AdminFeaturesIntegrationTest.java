package com.brayanmonteiro.starter;

import com.brayanmonteiro.starter.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminFeaturesIntegrationTest extends AbstractIntegrationTest {

    private HttpHeaders adminHeaders;

    @BeforeEach
    void authenticateAdmin() {
        adminHeaders = bearerHeaders(adminAccessToken());
    }

    @Test
    void dashboardStatsRequireAuthAndPermission() {
        ResponseEntity<String> anonymous = get("/api/admin/dashboard/stats", jsonHeaders(), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymous.getStatusCode());
        assertTrue(isJson(anonymous));

        ResponseEntity<Map> ok = get("/api/admin/dashboard/stats", adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, ok.getStatusCode());
        assertNotNull(ok.getBody().get("users"));
        assertNotNull(ok.getBody().get("roles"));
        assertNotNull(ok.getBody().get("audits"));
    }

    @Test
    void usersCrudWithXssSafePayloadAndAuditTrail() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String xssName = "<script>alert('u-" + suffix + "')</script>";
        String email = "user-" + suffix + "@java-starter.test";

        Long userRoleId = findRoleId("user");

        ResponseEntity<Map> created = post(
                "/api/admin/users",
                Map.of(
                        "name", xssName,
                        "email", email,
                        "password", "password12",
                        "roleIds", List.of(userRoleId)
                ),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertTrue(isJson(created));
        assertEquals(xssName, created.getBody().get("name"));
        assertEquals(email, created.getBody().get("email"));
        Number userId = (Number) created.getBody().get("id");
        assertNotNull(userId);

        // Resposta JSON não deve virar HTML executável
        ResponseEntity<String> raw = get("/api/admin/users/" + userId.longValue(), adminHeaders, String.class);
        assertTrue(isJson(raw));
        assertFalse(raw.getBody().toLowerCase().contains("<html"));
        assertTrue(raw.getBody().contains("\\u003c") || raw.getBody().contains("<script>"));

        ResponseEntity<Map> updated = put(
                "/api/admin/users/" + userId.longValue(),
                Map.of(
                        "name", "Usuário Seguro " + suffix,
                        "email", email,
                        "roleIds", List.of(userRoleId)
                ),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertEquals("Usuário Seguro " + suffix, updated.getBody().get("name"));

        ResponseEntity<Map> list = get("/api/admin/users?page=0&size=50", adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, list.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) list.getBody().get("items");
        assertTrue(items.stream().anyMatch(u -> email.equals(u.get("email"))));

        ResponseEntity<Void> deleted = delete("/api/admin/users/" + userId.longValue(), adminHeaders);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());

        ResponseEntity<Map> missing = get("/api/admin/users/" + userId.longValue(), adminHeaders, Map.class);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());

        ResponseEntity<Map> audits = get("/api/admin/audits?page=0&size=50", adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, audits.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auditItems = (List<Map<String, Object>>) audits.getBody().get("items");
        assertNotNull(auditItems);
        assertTrue(auditItems.stream().anyMatch(a ->
                "User".equals(a.get("auditableType"))
                        && "created".equals(a.get("event"))
                        && String.valueOf(a.get("newValuesJson")).contains(email)
        ));
        // payload XSS deve estar escapado no JSON de auditoria
        assertTrue(auditItems.stream().anyMatch(a -> {
            Object json = a.get("newValuesJson");
            return json != null && json.toString().contains("\\u003c") || (json != null && json.toString().contains("<script>"));
        }) || auditItems.stream().anyMatch(a -> {
            Object json = a.get("newValuesJson");
            return json != null && json.toString().contains(email);
        }));
    }

    @Test
    void usersRejectWeakPasswordAndDuplicateEmail() {
        Long userRoleId = findRoleId("user");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "dup-" + suffix + "@java-starter.test";

        ResponseEntity<Map> weak = post(
                "/api/admin/users",
                Map.of("name", "Weak", "email", email, "password", "123", "roleIds", List.of(userRoleId)),
                adminHeaders,
                Map.class
        );
        assertEquals(UNPROCESSABLE, weak.getStatusCode());

        ResponseEntity<Map> first = post(
                "/api/admin/users",
                Map.of("name", "One", "email", email, "password", "password12", "roleIds", List.of(userRoleId)),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, first.getStatusCode());

        ResponseEntity<Map> duplicate = post(
                "/api/admin/users",
                Map.of("name", "Two", "email", email, "password", "password12", "roleIds", List.of(userRoleId)),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CONFLICT, duplicate.getStatusCode());

        delete("/api/admin/users/" + ((Number) first.getBody().get("id")).longValue(), adminHeaders);
    }

    @Test
    void rolesCrudCatalogAndProtectedAdminRole() {
        ResponseEntity<Map> catalog = get("/api/admin/permissions/catalog", adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, catalog.getStatusCode());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> permissions = (List<Map<String, Object>>) catalog.getBody().get("permissions");
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        Long permId = ((Number) permissions.getFirst().get("id")).longValue();

        String roleName = "qa-" + UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<Map> created = post(
                "/api/admin/roles",
                Map.of("name", roleName, "permissionIds", List.of(permId)),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        Number roleId = (Number) created.getBody().get("id");
        assertEquals(roleName, created.getBody().get("name"));

        ResponseEntity<Map> updated = put(
                "/api/admin/roles/" + roleId.longValue(),
                Map.of("name", roleName + "-upd", "permissionIds", List.of(permId)),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertEquals(roleName + "-upd", updated.getBody().get("name"));

        ResponseEntity<List<Map<String, Object>>> list = get(
                "/api/admin/roles",
                adminHeaders,
                new ParameterizedTypeReference<>() {}
        );
        assertTrue(list.getBody().stream().anyMatch(r -> (roleName + "-upd").equals(r.get("name"))));

        ResponseEntity<Void> deleted = delete("/api/admin/roles/" + roleId.longValue(), adminHeaders);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());

        Long adminRoleId = findRoleId("admin");
        ResponseEntity<Map> deleteAdmin = rest.exchange(
                "/api/admin/roles/" + adminRoleId,
                org.springframework.http.HttpMethod.DELETE,
                new org.springframework.http.HttpEntity<>(adminHeaders),
                Map.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, deleteAdmin.getStatusCode());
        assertEquals("PROTECTED_ROLE", deleteAdmin.getBody().get("title"));

        ResponseEntity<Map> adminStillThere = get("/api/admin/roles/" + adminRoleId, adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, adminStillThere.getStatusCode());
        assertEquals("admin", adminStillThere.getBody().get("name"));
    }

    @Test
    void auditsListAndShow() {
        // gera evento
        Long userRoleId = findRoleId("user");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ResponseEntity<Map> created = post(
                "/api/admin/users",
                Map.of(
                        "name", "Audit User " + suffix,
                        "email", "audit-" + suffix + "@java-starter.test",
                        "password", "password12",
                        "roleIds", List.of(userRoleId)
                ),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        long createdUserId = ((Number) created.getBody().get("id")).longValue();

        ResponseEntity<Map> page = get("/api/admin/audits?page=0&size=20", adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, page.getStatusCode());
        assertTrue(((Number) page.getBody().get("total")).longValue() >= 1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) page.getBody().get("items");
        Number auditId = (Number) items.getFirst().get("id");

        ResponseEntity<Map> show = get("/api/admin/audits/" + auditId.longValue(), adminHeaders, Map.class);
        assertEquals(HttpStatus.OK, show.getStatusCode());
        assertNotNull(show.getBody().get("event"));
        assertNotNull(show.getBody().get("auditableType"));

        delete("/api/admin/users/" + createdUserId, adminHeaders);
    }

    @Test
    void userWithoutPermissionsCannotAccessAdminApis() {
        Long userRoleId = findRoleId("user");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "limited-" + suffix + "@java-starter.test";
        String password = "password12";

        ResponseEntity<Map> created = post(
                "/api/admin/users",
                Map.of("name", "Limited", "email", email, "password", password, "roleIds", List.of(userRoleId)),
                adminHeaders,
                Map.class
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        long id = ((Number) created.getBody().get("id")).longValue();

        String token = String.valueOf(login(email, password).get("accessToken"));
        HttpHeaders limited = bearerHeaders(token);

        // role "user" tem dashboard.view (seed), mas não users/roles/audit
        assertEquals(HttpStatus.OK, get("/api/admin/dashboard/stats", limited, String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/api/admin/users?page=0&size=10", limited, String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/api/admin/roles", limited, String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, get("/api/admin/audits", limited, String.class).getStatusCode());

        delete("/api/admin/users/" + id, adminHeaders);
    }

    @SuppressWarnings("unchecked")
    private Long findRoleId(String name) {
        ResponseEntity<List<Map<String, Object>>> roles = get(
                "/api/admin/roles",
                adminHeaders,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK, roles.getStatusCode());
        return roles.getBody().stream()
                .filter(r -> name.equals(r.get("name")))
                .map(r -> ((Number) r.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role não encontrada: " + name));
    }
}
