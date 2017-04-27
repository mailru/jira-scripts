import com.atlassian.jira.component.ComponentAccessor

def getGroup(String groupName){
    ComponentAccessor.getGroupManager().getGroup(groupName)
}

def isUserInGroup(user, group){
	ComponentAccessor.getGroupManager().isUserInGroup(user, group)
}

def isUserInGroup(String userName, String groupName){
	def gm = ComponentAccessor.getGroupManager()
	gm.isUserInGroup(getUserByName(userName), gm.getGroup(groupName))
}

def isUserInGroup(user, group){
    ComponentAccessor.getGroupManager().isUserInGroup(user, group)
}

def isUserInGroup(String userName, String groupName){
    ComponentAccessor.getGroupManager().with{
        isUserInGroup(getUserByName(userName), getGroup(groupName))
    }
}

def isUserInGroup(user, String groupName){
    ComponentAccessor.getGroupManager().with{
        isUserInGroup(user, getGroup(groupName))
    }
}

def isCurrentUserInGroup(group){
	ComponentAccessor.getGroupManager().isUserInGroup(getCurrentUser(), group)
}

def isCurrentUserInGroup(String groupName){
    ComponentAccessor.getGroupManager().with{
        isUserInGroup(getCurrentUser(), getGroup(groupName))
    }
}

def addUserInGroup(user, group){
	ComponentAccessor.getGroupManager().addUserToGroup(user, group)
}

def addUserInGroup(String userName, String groupName){
	def gm = ComponentAccessor.getGroupManager()
	gm.addUserToGroup(getUserByName(userName), gm.getGroup(groupName))
}

def getAllGroups(){
	ComponentAccessor.getGroupManager().getAllGroups()
}

def deleteUserFromGroup(user, group){
	//todo
}

