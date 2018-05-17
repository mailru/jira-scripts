import com.atlassian.jira.component.ComponentAccessor

def getOptionsFor(customField, issue) {
    ComponentAccessor.getOptionsManager().getOptions(customField.getRelevantConfig(issue))
}

def getOptionsByValue(String value) {
    ComponentAccessor.getOptionsManager().findByOptionValue(value)
}

def getOptionById(Long id) {
    ComponentAccessor.getOptionsManager().findByOptionId(id)
}

def deleteOption(option) {
    //todo
}

def disableOption(option) {
    //todo
}

def enableOption(option) {
    //todo
}

def createNewOption(customField, Long schemesId, String value) {
    def newOption = null;
    if (customField != null) {
        def schemes = customField.getConfigurationSchemes();
        if (schemes != null && !schemes.isEmpty()) {
            def sc = schemes.find { it.id == schemesId };
            def configs = sc.getConfigsByConfig();
            if (configs != null && !configs.isEmpty()) {
                def config = configs.keySet().iterator().next();
                def optionsManager = ComponentAccessor.getOptionsManager();
                def l = optionsManager.getOptions(config);
                int nextSequence = l.isEmpty() ? 1 : l.getRootOptions().size() + 1;
                newOption = optionsManager.createOption(config, null, (long) nextSequence, value);
            }
        }
    }
}
