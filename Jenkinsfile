//library('common-tools-SL')
import java.time.*
import groovy.json.JsonOutput
import com.nirima.jenkins.plugins.docker.DockerCloud
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import hudson.tools.ToolLocationNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty
import groovy.xml.MarkupBuilder

// GLOBAL VARIABLES
def htmlReport
def builder

pipeline {
  agent any
  stages {
    stage('Test') {
      steps {
        //ParseDockertemplates() //call template
        script{
          Date date = new Date()
          Jenkins.instance.setSystemMessage("Message: ${date}")
          //Set CSP header
          System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-scripts allow-same-origin; default-src 'self' ;img-src 'self' data; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval';")
          def result = getDockerTemplates()
          //Testing(JsonOutput.toJson(result))
          Testing(parseJsonForXml())
          //println(parseJsonForXml())
        }
      }
    }
  }
}

@NonCPS
def Testing(content){
  new File("${JENKINS_HOME}/userContent/test.html").withWriter('UTF-8') { writer ->
    try {
      writer.write(content.toString())
    } finally {
      writer.close()
    }
  }
  return
}

//get docker templates from the Jenkins instance
@NonCPS
def getDockerTemplates(){
  def jenkins = Jenkins.instance
  def data = ['nodes':[], 'dockerCloud':[], 'kubernetesCloud':[]]

  //get nodes from Jenkins instance
  def nodes = jenkins.nodes

  //get clouds from Jenkins instance
  def dockerClouds = jenkins.clouds.findAll({ it instanceof DockerCloud })
  def kubernetesClouds = jenkins.clouds.findAll({ it instanceof KubernetesCloud })

  //Jenkins nodes
  nodes.each{ node ->
    def computer = node.computer
    def online = computer.online ? 'Online' : 'Offline'
    def os = null
    if (computer.channel) { os = computer.unix ? 'Unix' : 'Windows' }
    def envVars = node.nodeProperties.find({ it instanceof EnvironmentVariablesNodeProperty })?.envVars
    def toolsLocation = getToolLocations( node.nodeProperties.find({ it instanceof ToolLocationNodeProperty })?.locations )
    def launcher_type, hostname = ''
    if (node.launcher instanceof hudson.plugins.sshslaves.SSHLauncher) {
      launcher_type = 'SSH'
      hostname = node.launcher.host
    } else if (node.launcher instanceof hudson.slaves.JNLPLauncher) {
      launcher_type = 'JNLP'
      hostname = node.displayName
    }
    data.nodes += ['labels':node.labelString, 'name':node.name, 'launcher': launcher_type, 'hostname':hostname, 'instanceCap':node.numExecutors, 'os':os, 'status':online, 'envVars':envVars, 'toolsLocation':toolsLocation]
  }
  //DockerCloud
  dockerClouds.each{ cloud ->
  def templates = []
  cloud.templates.each{ template ->
    def envVars = template.nodeProperties.find({ it instanceof EnvironmentVariablesNodeProperty })?.envVars
    def toolsLocation = getToolLocations( template.nodeProperties.find({ it instanceof ToolLocationNodeProperty })?.locations )
    templates += ['labels':template.labelString, 'name':template.name, 'image':template.image, 'instanceCap':template.instanceCap, 'disabled':template.disabled.isDisabled(), 'envVars':envVars, 'toolsLocation':toolsLocation]
  }
  data.dockerCloud += ["$cloud.name":['hostname': cloud.dockerApi.dockerHost.uri.tokenize('/')[1].tokenize(':')[0], 'templates': templates]]
  }
  //KubernetesCloud
  kubernetesClouds.each{ cloud ->
  def templates = []
  cloud.templates.each{ template ->
    def toolsLocation = getToolLocations( template.nodeProperties.find({ it instanceof ToolLocationNodeProperty })?.locations )
    templates += ['labels':template.label, 'name':template.name, 'image':template.image, 'instanceCap':template.instanceCap, 'envVars':template.envVars?template.envVars:null, 'toolsLocation':toolsLocation]
  }
  data.kubernetesCloud += ["$cloud.name":['hostname': cloud.serverUrl.tokenize('/')[1].tokenize(':')[0], 'templates': templates]]
  }
  return data
}

//get tools from template configuration and order by tool type
@NonCPS
def getToolLocations(toolLocations){
  def toolsLocation=toolLocations?.groupBy{it.type.displayName}
  toolsLocation.each { type, tools -> toolsLocation[type] = tools.collect {['home':it.home, 'name':it.name]} }
  return toolsLocation
}

//create HTML report file
@NonCPS
def parseJsonForXml() {
  htmlReport = new StringWriter()
  builder = new MarkupBuilder(htmlReport)
  tools = getDockerTemplates()
  builder.with {
    html {
      head {
        title('Stylish Table Page')
        style (getCSS())
      }
      //Main content
      body {
        header {
          h1('Jenkins')
        }
        //Header
        div(class: 'header') {
          //Home button
          a(href: "${JENKINS_URL}") {
            button(type: 'button', 'Return to home page')
          }
        }
        div(class: 'header') {
          p('Hello, this HTML report is very intersting...')
        }
        //Jenkins nodes
        div(class: 'entry-type') {
          h2(class: 'entry-type-title', 'Jenkins nodes')
          div(class: 'container') {
            //nodes
            tools.nodes.each{ node ->
              addNode(
                [
                  ['Labels':node.labels],
                  ['Name':node.name],
                  ['Launcher type':node.launcher],
                  ['Hostname':node.hostname],
                  ['Executors':node.instanceCap],
                  ['Operating system':node.os],
                  ['Status':node.status],
                  ['Tools':node.toolsLocation],
                  ['Environment variables':node.envVars]
                ]
              )
            } //end nodes
          }
        } //end Jenkins nodes
        //Clouds (Docker & Kubernetes)
        addCloud(tools.dockerCloud, "Docker")
        addCloud(tools.kubernetesCloud, "Kubernetes")
        div(class: 'footer') {
          h1('Bye bye')
        }
      }
    }
  }
  return htmlReport
}

//Function to add Jenkins clouds infos
@NonCPS
def addCloud(clouds, String name) {
  builder.with {
    div(class: 'entry-type') {
      h2(class: 'entry-type-title', "${name} template(s)")
      clouds.each{ cloudEntry ->
        cloudEntry.each{ cloud ->
          div(class: 'container') {
            p("Running on ${cloud.key} - hostname: ${cloud.value.hostname}")
            //templates
            cloud.value.templates.each{ template ->
              addNode(
                [
                  ['Labels':template.labels],
                  ['Name':template.name],
                  ['Docker image':template.image],
                  ['Instance capacity':template.instanceCap],
                  ['Tools':template.toolsLocation],
                  ['Environment variables':template.envVars]
                ]
              )
            } //end nodes
          }
        }
      }
    }
  }
}

// Function to add a table with tools details
@NonCPS
def addNode(details) {
  builder.with {
    table(class: 'entry') {
      thead {
        tr {
          th('Name')
          th('Details')
        }
      }
      //general details
      tbody {
        details.each { mapEntry ->
          mapEntry.each{ detail ->
            if (detail.value) {
              tr {
                switch(detail.key) {
                  case 'Labels':
                    td(class: 'properties', "${detail.key}")
                    td {
                      detail.value.tokenize(' ').each{ label ->
                        a(href: "${JENKINS_URL}label/${label}/", "${label}")
                      }
                    }
                    break
                  case 'Docker image':
                    td(class: 'properties', "${detail.key}")
                    String image = detail.value
                    String SDP_REGISTRY_ID = 'sf'
                    String SDP_REGISTRY_SUFFIX = '-docker-registry'
                    String SDP_REGISTRY = SDP_REGISTRY_ID+SDP_REGISTRY_SUFFIX
                    if (image.startsWith(SDP_REGISTRY)) {
                      def config = [
                        'serverName':'sec-nexus-01',
                        'domainName':'devops.in.idemia.com',
                        'contextURL':'/nexus',
                        'repositoryURL':'service/local/repositories',
                        'repositoryID':'mph-nexus-01-mogl-release',
                        'artifactURL':'content'
                      ]
                      String group = image.substring(image.indexOf('/')+1, image.lastIndexOf('/'))
                      String artifact = image.substring(image.lastIndexOf('/')+1, image.indexOf(':'))
                      String version = image.split(':', 2)[1]
                      String imageDocumentation = "https://${config.serverName}.${config.domainName}${config.contextURL}/${config.repositoryURL}/${config.repositoryID}/${config.artifactURL}/${group}/${artifact}/${version}/${artifact}-${version}.html"
                      td {
                        div(image)
                        div(class: 'image-documentation') { a(href: "${imageDocumentation}", 'Docker image documentation') }
                      }
                      //td("${detail.value}")
                    } else {
                      td("${detail.value}")
                    }
                    break
                  //tools
                  case 'Tools':
                    td('Tools')
                    addTools(detail.value)
                    break
                  //envVars
                  case 'Environment variables':
                    td('Environment variables')
                    addEnvVars(detail.value)
                    break
                  default:
                    td(class: 'properties', "${detail.key}")
                    td("${detail.value}")
                    break
                }
              }
            }
          }
        }
      }
    }
  }
}

// Function to add a table with tools details
@NonCPS
def addTools(tools) {
  builder.with {
    td {
      tools.each { toolName, toolData -> //tool type
        h4(class: 'tool-name', "${toolName}:")
        table(class: 'details-table') {
          tr {
            th('Name')
            th('Home')
          }
          toolData.each{ location -> //display all tools
            tr {
              td("${location.name}")
              td("${location.home}")
            }
          }
        }
      }
    }
  }
}

// Function to add a table env variables
@NonCPS
def addEnvVars(envVars) {
    builder.with {
      td {
      table(class: 'details-table') {
        tr {
          th('Name')
          th('Home')
        }
        envVars.each { env -> //display all env variables
          tr {
            td("${env.key}")
            td("${env.value}")
          }
        }
      }
    }
  }
}

//Function to return CSS style
@NonCPS
def getCSS(){
  return '''
  body {
    font-size: smaller;
    font-family: Verdana, sans-serif;
    background-color: #f6f6f6;
    margin: 0;
    padding: 0;
  }
  .container {
    margin: 10px auto;
    background-color: #fff;
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  }
  .header, .footer, .entry-type-title {
    margin: 10px auto;
    padding: 10px;
  }
  .container, .header, .footer, .entry-type-title {
    max-width: 950px;
  }
  header {
    background-color: #333;
    color: #fff;
    text-align: center;
    padding: 20px;
  }
  h1 {
    font-size: 2em;
  }
  .entry-type {
    margin-top: 20px;
    padding: 20px;
    border-radius: 10px;
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th, td {
    padding: 12px 15px;
    text-align: left;
    border: 1px solid black;
  }
  th {
    background-color: #444;
    color: #fff;
  }
  tr:nth-child(even) {
    background-color: #c7d7e6;
  }
  .entry {
    margin: 40px 0px;
  }
  .details-table {
    width: 100%;
    border-collapse: collapse;
  }
  .details-table th, .details-table td {
    padding: 6px 14px;
    text-align: left;
    width: 50%;
  }
  .details-table th {
    background-color: #555;
    color: #fff;
  }
  .details-table tr:nth-child(even) {
    background-color: #f6f6f6;
  }
  .details-table tr:nth-child(odd) {
    background-color: #e3ebf2;
  }
  .tool-name, .image-documentation {
    margin: 0 auto;
    padding: 6px 10px;
  }
  .properties {
    width: 30%;
  }
  button {
    background-color: #0074d9;
    color: #fff;
    border: none;
    border-radius: 5px;
  }
  button {
    padding: 10px 20px;
  }
  button:hover {
    background-color: #0056b3;
  }
'''
}
