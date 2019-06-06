 Universal setting of task sending to status "Need info" from any initial status of the status and return to the initial status.
 1) create new customField with type Number - it will store the transition id for the return from "Need info" to source status.
 We recommend making this field with a global context for reuse in any projects.
 2) Set variable cfIdTransition in scripts "send to need info" and "return in previous status". Replace 10000 your customField id.
 3) Add scripts "send to need info" and "return in previous status" to registers for convenient access and reuse in any workflow.
 4) Add script "send to need info" as post-function to all transitions in status "Need info" in workflow.
 5) Make a return transition from the status "Need info" to the workflow.
 In each transition from need info add "condition" - "return in previous status"
 6) save workflow.



