issue.setAssignee(getAssigneeFromComponents())

def getAssigneeFromComponents() {
    def components = issue.getComponents()
    if (!components) return null
    def firstComponentWithLead = components.find { it.getComponentLead() }
    return firstComponentWithLead.getComponentLead()
}