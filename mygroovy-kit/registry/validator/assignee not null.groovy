import com.opensymphony.workflow.InvalidInputException

if (issue.getAssignee()) return
invalidInputException = new InvalidInputException()
invalidInputException.addError("Необходимо назначить исполнителя")
throw invalidInputException