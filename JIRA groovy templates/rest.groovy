import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import groovy.json.JsonSlurper

import javax.xml.bind.DatatypeConverter
import java.io.IOException
import java.net.HttpURLConnection

baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
def userName = "login"
def password = "pass"

def remoteUrl = 'https://jira.ru/'

def issueKey = 'KEY-1'

getRemoteIssue(remoteUrl,
        userName,
        password,
        issueKey)?.fields?.status

def getRemoteIssue(String baseUrl,
                   String userName,
                   String password,
                   String issueKey) {
    def authString = ("${userName}:${password}").getBytes().encodeBase64().toString()
    def connection = ("${baseUrl}rest/api/2/issue/${issueKey}").toURL().openConnection()
    connection.addRequestProperty('Authorization', 'Basic ' + authString)
    connection.addRequestProperty('Content-Type', 'application/json')
    connection.setRequestMethod('GET')
    connection.setReadTimeout(30000)
    try {
        connection.connect()
        def line = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(line)

    } finally {
        connection.disconnect();
    }
}


def url = "${baseUrl}/rest/api/2/issue/${issueKey}"
def content = """
       {
           "fields": {
              "priority": { "id": "${priorityId}" }
           }
       }
   """
put(url, content, userName, password)

def put(String url, String content, String userName, String password) {
    String authString = "${userName}:${password}".getBytes().encodeBase64().toString()
    def connection = url.toURL().openConnection()
    connection.addRequestProperty("Authorization", "Basic ${authString}")
    connection.addRequestProperty("Content-Type", "application/json")
    connection.setReadTimeout(30000)
    connection.setRequestMethod("PUT")
    connection.doOutput = true
    connection.outputStream.withWriter {
        it.write(content)
        it.flush()
    }

    try {
        connection.connect()
        def line = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(line)
    } catch (IOException e) {
        try {
            ((HttpURLConnection) connection).errorStream.text
        } catch (Exception ignored) {
            //throw e
        }
    } finally {
        connection.disconnect();
    }
}

def get(String url, String userName, String password) {
    String authString = "${userName}:${password}".getBytes().encodeBase64().toString()
    def connection = url.toURL().openConnection()
    connection.addRequestProperty("Authorization", "Basic ${authString}")
    connection.addRequestProperty("Content-Type", "application/json")
    connection.setReadTimeout(30000)
    connection.setRequestMethod("GET")

    try {
        connection.connect()
        def line = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine()
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(line)
    } catch (IOException e) {
        try {
            ((HttpURLConnection) connection).errorStream.text
        } catch (Exception ignored) {
            //throw e
        }
    }
}

url = "rest/test/${param}"
Sender sender = new Sender(host: "test.ru",
        port: 666,
        user: "login",
        password: "pass",
        isSecure: true);
sender.call(url)


def class Sender {
    String host;
    int port;
    String user;
    String password;
    boolean isSecure;

    def String getAuthRealm() {
        return DatatypeConverter.printBase64Binary(user.concat(":").concat(password).getBytes());
    }

    def call(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) """${isSecure ? 'https' : 'http'}://${host}:${port}/${
            url
        }""".toURL().openConnection(Proxy.NO_PROXY)

        try {
            connection.setReadTimeout(30000)
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setAllowUserInteraction(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Authorization", "Basic " + getAuthRealm());

            int rc = connection.getResponseCode();
            if (rc == HttpURLConnection.HTTP_OK) {
                //return IOUtils.toString(connection.getInputStream(), "UTF-8");
                def reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                def line = ""
                while (reader.ready()) {
                    line += reader.readLine()
                }
                def jsonSlurper = new JsonSlurper()
                return jsonSlurper.parseText(line)
            } else
                throw new IOException(String.format("Bad response, code=%d, message=%s", rc, connection.getErrorStream() != null ? IOUtils.toString(connection.getErrorStream(), "UTF-8") : ""));
        } finally {
            connection.disconnect();
        }
    }
}


def get(String url) {
    def jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText(url.toURL().text)
}
