import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.login.LoginManager
import com.atlassian.jira.user.UserUtils

return getActiveUsersHaveNotUsedLogin(365).size()

def getCurrentUser(){
    ComponentAccessor.jiraAuthenticationContext?.user
}

def getUserByName(String userName){
    ComponentAccessor.userManager.getUserByName(userName)
}

def getAllUsers(){
    ComponentAccessor.userManager.getAllUsers()
}

def getAllUsersHaveNotUsedLogin(Long days){
    def lm = ComponentAccessor.getComponentOfType(LoginManager.class)
    def users = getAllUsers()
   	def daysInMillis = days*24*60*60*1000
    def nowMillis = new Date().time
    users.findAll{user->
        nowMillis - daysInMillis >= lm.getLoginInfo(user.name).lastLoginTime
    }
}

def getActiveUsersHaveNotUsedLogin(Long days){
    getAllUsersHaveNotUsedLogin(days).findAll{it.isActive()}
}

def findUserByEmail(String email){
    UserUtils.getUserByEmail(assignees.output.cfo)
}

def findUserByLastName(String lastName){
    //todo
}

def disableUser(user){
    //todo
}


