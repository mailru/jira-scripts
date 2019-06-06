import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.config.properties.ApplicationProperties
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.hash.Hashing
import ru.mail.jira.plugins.groovy.api.dto.directory.RegistryScriptDto
import ru.mail.jira.plugins.groovy.api.repository.ScriptRepository
import ru.mail.jira.plugins.groovy.api.script.PluginModule
import ru.mail.jira.plugins.groovy.api.script.StandardModule

import java.nio.charset.StandardCharsets

@PluginModule
ScriptRepository scriptRepository
@StandardModule
ApplicationProperties applicationProperties

Multimap<String, RegistryScriptDto> result = HashMultimap.create()

scriptRepository
        .getAllScripts()
        .forEach { result.put(Hashing.murmur3_128().hashString(it.scriptBody, StandardCharsets.UTF_8).toString(), it) }

result
        .keySet()
        .findAll { result.get(it).size() > 1 }
        .collect {
    result
            .get(it)
            .collect {
        "<a href=\"${applicationProperties.getText(APKeys.JIRA_BASEURL)}/plugins/servlet/my-groovy/registry/script/view/${it.id}\">${it.name}</a>"
    }
}
.join("<br/>-----<br/>")
