import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager


def getAllRoles() {
    def prm = ComponentAccessor.getComponent(ProjectRoleManager)
    prm.getProjectRoles()
}

def getProjectRole(String projectRoleName) {
    def prm = ComponentAccessor.getComponent(ProjectRoleManager)
    prm.getProjectRole(projectRoleName)
}


def getUsersOnProjectRole(project, role) {
    def prm = ComponentAccessor.getComponent(ProjectRoleManager)
    prm.getProjectRoleActors(role, project)?.getApplicationUsers()
}

def getRolesForUser(user, project) {
    def prm = ComponentAccessor.getComponent(ProjectRoleManager)
    prm.getProjectRoles(user, project)
}

def isUserInProjectRole(user, projectRole, project) {
    def prm = ComponentAccessor.getComponent(ProjectRoleManager)
    prm.isUserInProjectRole(user, projectRole, project)
}
