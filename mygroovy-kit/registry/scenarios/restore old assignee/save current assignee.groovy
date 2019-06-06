//THIS IS SAVE CURRENT ASSIGNEE SCRIPT
long FIELD_ID = 10200;
def cfOldAssignee = _.getCustomFieldObject(FIELD_ID)

def currentAssignee = issue.assignee
if (currentAssignee) {
    issue.setCustomFieldValue(cfOldAssignee, currentAssignee);
} else {
    issue.setCustomFieldValue(cfOldAssignee, currentUser);
}