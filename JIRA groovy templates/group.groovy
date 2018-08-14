import com.atlassian.jira.component.ComponentAccessor

def isUserInGroup(user, String groupName) {
    ComponentAccessor.getGroupManager().with {
        isUserInGroup(user, getGroup(groupName))
    }
}

def getGroup(String groupName) {
    ComponentAccessor.getGroupManager().getGroup(groupName)
}

def getUsersInGroup(group) {
    ComponentAccessor.getGroupManager().getUsersInGroup(group)
}

def getUsersInGroup(String groupName) {
    ComponentAccessor.getGroupManager().with {
        getUsersInGroup(getGroup(groupName))
    }
}

def isUserInGroup(user, group) {
    ComponentAccessor.getGroupManager().isUserInGroup(user, group)
}

def isUserInGroup(String userName, String groupName) {
    ComponentAccessor.getGroupManager().with {
        isUserInGroup(getUserByName(userName), getGroup(groupName))
    }
}

def isCurrentUserInGroup(group) {
    ComponentAccessor.getGroupManager().isUserInGroup(getCurrentUser(), group)
}

def isCurrentUserInGroup(String groupName) {
    ComponentAccessor.getGroupManager().with {
        isUserInGroup(getCurrentUser(), getGroup(groupName))
    }
}

def addUserInGroup(user, group) {
    ComponentAccessor.getGroupManager().addUserToGroup(user, group)
}

def addUserInGroup(String userName, String groupName) {
    def groupManager = ComponentAccessor.getGroupManager()
    groupManager.addUserToGroup(getUserByName(userName), groupManager.getGroup(groupName))
}

def getAllGroups() {
    ComponentAccessor.getGroupManager().getAllGroups()
}

def getGroupsForUser(user) {
    ComponentAccessor.getUserUtil().getGroupsForUser(user)
}

def deleteUserFromGroups(user, groups) {
    ComponentAccessor.getUserUtil().removeUserFromGroups(groups, user)
}

def deleteUserFromAllGroups(user) {
    deleteUserFromGroups(user, getGroupsForUser(user))
}
