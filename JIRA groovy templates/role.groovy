import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager


def getAllRoles() {
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    projectRoleManager.getProjectRoles()
}

def getProjectRole(String projectRoleName) {
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    projectRoleManager.getProjectRole(projectRoleName)
}


def getUsersOnProjectRole(project, role) {
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    projectRoleManager.getProjectRoleActors(role, project)?.getApplicationUsers()
}

def getRolesForUser(user, project) {
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    projectRoleManager.getProjectRoles(user, project)
}

def isUserInProjectRole(user, projectRole, project) {
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    projectRoleManager.isUserInProjectRole(user, projectRole, project)
}
