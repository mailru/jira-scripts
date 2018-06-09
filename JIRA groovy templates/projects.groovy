import com.atlassian.jira.component.ComponentAccessor;

def getAllProjects() {
    ComponentAccessor.projectManager.getProjectObjects()
}

def getProjectByKey(String key) {
    ComponentAccessor.projectManager.getProjectObjByKey(key)
}

def getProjectById(Long id) {
    ComponentAccessor.projectManager.getProjectObj(id)
}
