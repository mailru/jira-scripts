import com.atlassian.jira.component.ComponentAccessor;

def getComponens(project) {
    ComponentAccessor.projectComponentManager.findAllForProject(project.id)
}

def getComponens(Long projectId) {
    ComponentAccessor.projectComponentManager.findAllForProject(projectId)
}

def getComponent(String componentName, Long projectId) {
    ComponentAccessor.projectComponentManager.findByComponentName(projectId, componentName)
}

def getComponent(Long id) {
    ComponentAccessor.projectComponentManager.find(id)
}

def hasComponents(issue) {
    if (issue.components == null || issue.components.size() == 0) {
        return false
    } else {
        return true
    }
}

def hasComponent(issue, component) {
    issue.components.find { it == component } != null
}

def hasComponent(issue, String componentName) {
    issue.components.find { it.name == componentName } != null
}

def hasComponent(issue, Long componentId) {
    issue.components.find { it.id == componentId } != null
}

def hasComponentFromList(issue, List<String> componentNames) {
    issue.components.any { it.name in componentNames }
}

def hasComponentFromList(issue, List components) {
    issue.components.any { it in components }
}
