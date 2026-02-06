package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fw.ModelView;
import com.fw.UploadedFile;
import com.fw.annotations.AnnotationController;
import com.fw.annotations.ManageUrl;
import com.fw.annotations.MyGET;
import com.fw.annotations.MyPOST;
import com.fw.annotations.JSON;
import com.fw.annotations.Session;
import com.fw.annotations.Authorized;
import com.fw.annotations.Role;


@AnnotationController("/test")
public class Test {
    
    // Test for Map<String, Object> binding
    @ManageUrl("/mapparam")
    public String mapParam(Map<String, Object> params) {
        return "Map param: " + params;
    }

    @ManageUrl("/mapparam/{id}")
    public String mapParamPath(Map<String, Object> params) {
        return "Map param with path: " + params;
    }

    @ManageUrl("/bean")
    public String beanTest(Employee e, Department d) {
        return "Bean test: e=" + e + ", d=" + d;
    }

    @ManageUrl("/bean/single")
    public String beanSingle(Employee e) {
        return "Single bean: e=" + e;
    }

    @ManageUrl("/bean/list")
    public String beanList(List<Employee> employees, int popo) {
        return "Employee list: " + employees + ", popoList=" + popo;
    }

    @ManageUrl("/bean/array")
    public String beanArray(Employee[] employees, int popo) {
        return "Employee array: " + java.util.Arrays.toString(employees) + ", popoArray=" + popo;
    }

    @ManageUrl("/bean/nested")
    public String beanNested(Employee employee) {
        return "Employee with nested department: " + employee;
    }

    @ManageUrl("/bean/nested/list")
    public String beanNestedList(List<Employee> employees) {
        return "Employee list with nested departments: " + employees;
    }

    @ManageUrl("/bean/deep")
    @JSON
    public Employee beanDeep(Employee employee) {
        return employee;
    }
    
    @ManageUrl("/popo")
    public void popo() {

    }

    @ManageUrl("/mimi") 
    public void mimi() {
        
    }

    @ManageUrl("/hello")
    public String hello() {
        return "Hello from com.test.Test. If you see this text, action invocation works.";
    }

    @ManageUrl("/json/object")
    @JSON
    public Employee jsonEmployee() {
        Employee e = new Employee();
        e.setName("Alice");
        e.setAge(30);
        e.setEmail("alice@example.com");
        Department d = new Department();
        d.setLabel("Engineering");
        d.setFloor(3);
        e.setDepartment(d);
        return e;
    }

    @ManageUrl("/json/modelview")
    @JSON
    public ModelView jsonModelView() {
        ModelView mv = new ModelView(null);
        List<String> users = new ArrayList<>();
        users.add("Alice");
        users.add("Bob");
        mv.addItem("users", users);
        mv.addItem("count", users.size());
        return mv;
    }

    @ManageUrl("/mv")
    public ModelView mv() {
        List<String> users = new ArrayList<>();
        users.add("Alice");
        users.add("Bob"); 
        ModelView mv = new ModelView("user.jsp");
        mv.addItem("users", users);
        return mv;
    }

    @ManageUrl("/etudiant/{id}")
    public String etudiant(int id) {
       return "tongasoa " + id;
    }
    
    @ManageUrl("/etudiant/insert")
    public String insert(String name, Integer age) {
        return "Received form: name=" + (name == null ? "" : name) + ", age=" + (age == null ? "" : age);
    }

    @ManageUrl("/user/{id}")
    public String userById(int id) {
        return "User by id: " + id;
    }

    @ManageUrl("/user/{id}/{role}")
    public String userByIdRole(int id, String role) {
        return "User by id: " + id + ", role: " + role;
    }

    @ManageUrl("/user/{id}/{role}/{active}")
    public String userByIdRoleActive(int id, String role, boolean active) {
        return "User by id: " + id + ", role: " + role + ", active: " + active;
    }

    @ManageUrl("/multi/{popo}")
    @MyPOST
    public String multiPost(String popo) {
        return "POST multi: popo=" + popo;
    }

    // File upload tests
    @ManageUrl("/upload/single")
    @MyPOST
    public String uploadSingle(Map<String, UploadedFile> files) {
        if (files.isEmpty()) {
            return "No files uploaded";
        }
        
        StringBuilder sb = new StringBuilder("Uploaded files:\n");
        for (Map.Entry<String, UploadedFile> entry : files.entrySet()) {
            UploadedFile file = entry.getValue();
            sb.append("Field: ").append(entry.getKey())
              .append(", Filename: ").append(file.getOriginalFilename())
              .append(", Size: ").append(file.getSize()).append(" bytes")
              .append(", Type: ").append(file.getContentType())
              .append("\n");
        }
        return sb.toString();
    }

    @ManageUrl("/upload/with-params")
    @MyPOST
    public String uploadWithParams(Map<String, UploadedFile> files, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== Files ===\n");
        if (files.isEmpty()) {
            sb.append("No files uploaded\n");
        } else {
            for (Map.Entry<String, UploadedFile> entry : files.entrySet()) {
                UploadedFile file = entry.getValue();
                sb.append("Field: ").append(entry.getKey())
                  .append(", Filename: ").append(file.getOriginalFilename())
                  .append(", Size: ").append(file.getSize()).append(" bytes\n");
            }
        }
        
        sb.append("\n=== Parameters ===\n");
        if (params.isEmpty()) {
            sb.append("No parameters\n");
        } else {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }

    @ManageUrl("/upload/json")
    @MyPOST
    @JSON
    public ModelView uploadJson(Map<String, UploadedFile> files, String description) {
        ModelView mv = new ModelView();
        mv.addItem("description", description);
        mv.addItem("fileCount", files.size());
        
        List<Map<String, Object>> fileInfos = new ArrayList<>();
        for (Map.Entry<String, UploadedFile> entry : files.entrySet()) {
            UploadedFile file = entry.getValue();
            Map<String, Object> info = Map.of(
                "field", entry.getKey(),
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "contentType", file.getContentType()
            );
            fileInfos.add(info);
        }
        mv.addItem("files", fileInfos);
        
        return mv;
    }

    // Multiple files with same field name
    @ManageUrl("/upload/multiple")
    @MyPOST
    public String uploadMultiple(Map<String, List<UploadedFile>> files) {
        if (files.isEmpty()) {
            return "No files uploaded";
        }
        
        StringBuilder sb = new StringBuilder("Uploaded files (grouped by field):\n\n");
        for (Map.Entry<String, List<UploadedFile>> entry : files.entrySet()) {
            sb.append("Field: ").append(entry.getKey()).append("\n");
            List<UploadedFile> fileList = entry.getValue();
            sb.append("  Count: ").append(fileList.size()).append(" file(s)\n");
            for (int i = 0; i < fileList.size(); i++) {
                UploadedFile file = fileList.get(i);
                sb.append("  [").append(i).append("] ")
                  .append(file.getOriginalFilename())
                  .append(" (").append(file.getSize()).append(" bytes, ")
                  .append(file.getContentType()).append(")\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @ManageUrl("/upload/multiple-json")
    @MyPOST
    @JSON
    public ModelView uploadMultipleJson(Map<String, List<UploadedFile>> files, String category) {
        ModelView mv = new ModelView();
        mv.addItem("category", category);
        
        Map<String, Object> groupedFiles = new HashMap<>();
        int totalFiles = 0;
        
        for (Map.Entry<String, List<UploadedFile>> entry : files.entrySet()) {
            List<Map<String, Object>> fileInfos = new ArrayList<>();
            for (UploadedFile file : entry.getValue()) {
                fileInfos.add(Map.of(
                    "filename", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "contentType", file.getContentType()
                ));
                totalFiles++;
            }
            groupedFiles.put(entry.getKey(), Map.of(
                "count", entry.getValue().size(),
                "files", fileInfos
            ));
        }
        
        mv.addItem("totalFiles", totalFiles);
        mv.addItem("groups", groupedFiles);
        
        return mv;
    }

    // Session management tests
    @ManageUrl("/session/login")
    public String sessionLogin(@Session Map<String, Object> session, String username, String role) {
        // Store user info in session
        session.put("username", username);
        session.put("role", role);
        session.put("loginTime", System.currentTimeMillis());
        
        return "Login successful! Username: " + username + ", Role: " + role;
    }

    @ManageUrl("/session/info")
    public String sessionInfo(@Session Map<String, Object> session) {
        StringBuilder sb = new StringBuilder("Session Contents:\n");
        
        if (session.isEmpty()) {
            sb.append("Session is empty");
        } else {
            for (Map.Entry<String, Object> entry : session.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }

    @ManageUrl("/session/update")
    public String sessionUpdate(@Session Map<String, Object> session, String key, String value) {
        Object oldValue = session.put(key, value);
        return "Updated session: " + key + " = " + value + " (old value: " + oldValue + ")";
    }

    @ManageUrl("/session/remove")
    public String sessionRemove(@Session Map<String, Object> session, String key) {
        Object removed = session.remove(key);
        return "Removed from session: " + key + " = " + removed;
    }

    @ManageUrl("/session/clear")
    public String sessionClear(@Session Map<String, Object> session) {
        int size = session.size();
        session.clear();
        return "Cleared session. Removed " + size + " item(s)";
    }

    @ManageUrl("/session/json")
    @JSON
    public ModelView sessionJson(@Session Map<String, Object> session) {
        ModelView mv = new ModelView();
        mv.addItem("sessionData", new HashMap<>(session));
        mv.addItem("sessionSize", session.size());
        mv.addItem("isEmpty", session.isEmpty());
        return mv;
    }

    // Mixed usage: Session + regular parameters
    @ManageUrl("/session/counter")
    public String sessionCounter(@Session Map<String, Object> session) {
        Integer counter = (Integer) session.get("counter");
        if (counter == null) {
            counter = 0;
        }
        counter++;
        session.put("counter", counter);
        
        return "Page visits: " + counter;
    }

    // ==================== AUTHORIZATION & ROLE TESTS ====================

    /**
     * Login endpoint - sets user and roles in session
     */
    @ManageUrl("/auth/login")
    @MyPOST
    public String login(@Session Map<String, Object> session, String username, String role) {
        if (username == null || username.isEmpty()) {
            return "Error: Username is required";
        }
        
        session.put("user", username);
        
        // Parse roles (comma-separated)
        List<String> roles = new ArrayList<>();
        if (role != null && !role.isEmpty()) {
            String[] roleArray = role.split(",");
            for (String r : roleArray) {
                roles.add(r.trim());
            }
        }
        session.put("roles", roles);
        
        return "Logged in as: " + username + " with roles: " + roles;
    }

    /**
     * Logout endpoint - clears session
     */
    @ManageUrl("/auth/logout")
    public String logout(@Session Map<String, Object> session) {
        String username = (String) session.get("user");
        session.clear();
        return "Logged out: " + (username != null ? username : "anonymous");
    }

    /**
     * Check current login status
     */
    @ManageUrl("/auth/status")
    @JSON
    public ModelView authStatus(@Session Map<String, Object> session) {
        ModelView mv = new ModelView();
        mv.addItem("loggedIn", session.containsKey("user"));
        mv.addItem("user", session.get("user"));
        mv.addItem("roles", session.get("roles"));
        return mv;
    }

    /**
     * Public endpoint - no authorization required
     */
    @ManageUrl("/auth/public")
    public String publicEndpoint() {
        return "This is a public endpoint. Anyone can access this.";
    }

    /**
     * Protected endpoint - requires login only
     */
    @ManageUrl("/auth/protected")
    @Authorized
    public String protectedEndpoint(@Session Map<String, Object> session) {
        String user = (String) session.get("user");
        return "Welcome to protected area, " + user + "!";
    }

    /**
     * Admin only endpoint - requires admin role
     */
    @ManageUrl("/auth/admin")
    @Role("admin")
    public String adminEndpoint(@Session Map<String, Object> session) {
        String user = (String) session.get("user");
        List<?> roles = (List<?>) session.get("roles");
        return "Admin area. User: " + user + ", Roles: " + roles;
    }

    /**
     * Multiple roles - admin OR sale-manager can access
     */
    @ManageUrl("/auth/sales")
    @Role("admin, sale-manager")
    public String salesEndpoint(@Session Map<String, Object> session) {
        String user = (String) session.get("user");
        List<?> roles = (List<?>) session.get("roles");
        return "Sales area. User: " + user + ", Roles: " + roles;
    }

    /**
     * Manager only - requires manager role
     */
    @ManageUrl("/auth/manager")
    @Role("manager")
    @JSON
    public ModelView managerEndpoint(@Session Map<String, Object> session) {
        ModelView mv = new ModelView();
        mv.addItem("area", "manager");
        mv.addItem("user", session.get("user"));
        mv.addItem("roles", session.get("roles"));
        mv.addItem("message", "Welcome to manager dashboard");
        return mv;
    }

    /**
     * Case-insensitive role test
     */
    @ManageUrl("/auth/case-test")
    @Role("ADMIN")
    public String caseTestEndpoint() {
        return "Case-insensitive role check passed!";
    }
}


