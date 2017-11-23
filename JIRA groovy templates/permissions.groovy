import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.PermissionManager

def getProjectsWhereUserHasPermission(projectPermission, user) {
    def pm = ComponentAccessor.getComponentOfType(PermissionManager)
    pm.getProjects(projectPermission.getProjectPermissionKey(), user)
}

def getAllProjectPermissions() {
    def pm = ComponentAccessor.getComponentOfType(PermissionManager)
    pm.getAllProjectPermissions()
}

def getProjectPermissionByKey(String key) {
    getAllProjectPermissions().find { it.key == key }
}
