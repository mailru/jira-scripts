import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager

//role
def getAllRoles(){
	//todo
}

def getProjectRole(String projectRoleName){
	def prm = ComponentAccessor.getComponent(ProjectRoleManager)
	prm.getProjectRole(projectRoleName)
}

//role advance
def getUsersOnProjectRole(project, role){
	//todo
}

def getRolesForUser(user, project){
	//todo
}

def isUserInProjectRole(user, projectRole, project){
	def prm = ComponentAccessor.getComponent(ProjectRoleManager)
	prm.isUserInProjectRole(user, projectRole, project)
}
