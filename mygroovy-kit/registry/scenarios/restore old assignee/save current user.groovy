long FIELD_ID = 10200
def cfOldAssignee = _.getCustomFieldObject(FIELD_ID)
issue.setCustomFieldValue(cfOldAssignee, currentAssignee)