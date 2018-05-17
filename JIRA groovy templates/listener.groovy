//is field update?
def isCondition(event) {
    def fieldNames = ['Fix Version']
    return event.getChangeLog()?.getRelated("ChildChangeItem").find { it.get("field") in fieldNames } != null
}
