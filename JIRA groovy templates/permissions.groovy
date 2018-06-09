import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.PermissionManager

def getProjectsWhereUserHasPermission(projectPermission, user) {
    def permissionManager = ComponentAccessor.getComponentOfType(PermissionManager)
    permissionManager.getProjects(projectPermission.getProjectPermissionKey(), user)
}

def getAllProjectPermissions() {
    def permissionManager = ComponentAccessor.getComponentOfType(PermissionManager)
    permissionManager.getAllProjectPermissions()
}

def getProjectPermissionByKey(String key) {
    getAllProjectPermissions().find { it.key == key }
}
