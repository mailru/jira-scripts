import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.crowd.embedded.impl.ImmutableUser
import com.atlassian.jira.bc.user.UserService
import com.atlassian.jira.security.login.LoginManager
import com.atlassian.jira.user.ApplicationUsers
import com.atlassian.jira.user.UserUtils

disableUser(getUserByName('TESTUSERTESTUSER'))
//return getActiveUsersHaveNotUsedLogin(365).size()

def getCurrentUser() {
    ComponentAccessor.jiraAuthenticationContext?.user
}

def getUserByName(String userName) {
    ComponentAccessor.userManager.getUserByName(userName)
}

def getAllUsers() {
    ComponentAccessor.userManager.getAllUsers()
}

def getAllUsersHaveNotUsedLogin(Long days) {
    def loginManager = ComponentAccessor.getComponentOfType(LoginManager.class)
    def users = getAllUsers()
    def daysInMillis = days * 24 * 60 * 60 * 1000
    def nowMillis = new Date().time
    users.findAll { user ->
        nowMillis - daysInMillis >= loginManager.getLoginInfo(user.name).lastLoginTime
    }
}

def getActiveUsersHaveNotUsedLogin(Long days) {
    getAllUsersHaveNotUsedLogin(days).findAll { it.isActive() }
}

def findUserByEmail(String email) {
    UserUtils.getUserByEmail(email)
}

def findUsersByName(String partOfName) {
    ComponentAccessor.userManager.getAllUsers().findAll {
        it.getDisplayName().contains(partOfName)
    }
}

def disableUser(user) {
    def crowdService = ComponentAccessor.crowdService
    def userService = ComponentAccessor.getComponent(UserService)

    def userWithAttributes = crowdService.getUserWithAttributes(user.getName())
    def updateUser = ApplicationUsers.from(ImmutableUser.newUser(userWithAttributes).active(false).toUser())
    def updateUserValidationResult = userService.validateUpdateUser(updateUser)

    if (updateUserValidationResult.isValid()) {
        userService.updateUser(updateUserValidationResult)
    } else {
        return "Update of ${user.name} failed: ${updateUserValidationResult.getErrorCollection().getErrors().entrySet().join(',')}"
    }
}
