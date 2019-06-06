reporter = issue.getReporter()

if (issue.getAssignee()) return
issue.setAssignee(reporter)