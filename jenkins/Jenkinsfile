//library('common-tools-SL')
import java.time.*
import groovy.json.JsonOutput
import com.nirima.jenkins.plugins.docker.DockerCloud
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import hudson.tools.ToolLocationNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty
import groovy.xml.MarkupBuilder

pipeline {
  agent any
  stages {
    stage('Test') {
      steps {
        script{
          //Set CSP header
          System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-scripts allow-same-origin; default-src 'self' ;img-src 'self' data; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval';")
          publishReport(buildReport(getJenkinsNodes()), "${JENKINS_HOME}/userContent/test.html")
        }
      }
    }
  }
}

//Create file report inside userContent
@NonCPS
def publishReport(content, reportPath){
  new File(reportPath).withWriter('UTF-8') { writer ->
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }
  return
}

//Get Jenkins slaves from instance (nodes + templates)
@NonCPS
def getJenkinsNodes(){
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

//get tools from template configuration and group by tool type
@NonCPS
def getToolLocations(toolLocations){
  def toolsLocation=toolLocations?.groupBy{it.type.displayName}
  toolsLocation.each { type, tools -> toolsLocation[type] = tools.collect {['home':it.home, 'name':it.name]} }
  return toolsLocation
}

//create HTML report file
@NonCPS
def buildReport(tools) {
  def writer = new StringWriter()
  def html = new MarkupBuilder(writer)
  html.with {
    html {
      head {
        title('Stylish Table Page')
        style (getCSS())
      }
      //body content
      body {
        //main header
        header {
          h1('Jenkins - available tools')
        }
        //presentation
        div(class: 'header') {
          //home button
          a(href: "${JENKINS_URL}") {
            button(type: 'button', 'Return to home page')
          }
        }
        //welcome
        div(class: 'header') {
          h2('Welcome...')
          p('This automated report display available Jenkins slaves on your instance with associated informations. This page is also presenting which tools have been declared for each node/template.')
        }
        //request new CI slave
        div(class: 'header') {
          h3('Need new CI slave ?')
          p('Feel free to request a new CI slave on Jira and explain your needs.')
          em {
            p('Checkout our available docker images at: URL.')
          }
          //new CI slave
          a(href: "https://jira.url.com") {
            button(type: 'button', 'Request new CI slave')
          }
        }
        //Jenkins nodes
        if (tools.nodes){
          div(class: 'entry-type') {
            h2(class: 'entry-type-title', 'Jenkins nodes')
            div(class: 'container') {
              //nodes
              tools.nodes.each{ node ->
                addNode(
                  html,
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
        }
        //Clouds (Docker & Kubernetes)
        if (tools.dockerCloud){
          addCloud(html, tools.dockerCloud, "Docker")
        }
        if (tools.kubernetesCloud){
          addCloud(html, tools.kubernetesCloud, "Kubernetes")
        }
        //footer
        div(class: 'footer') {
          h1('Any questions ?')
          div {
            p('Contact us on Jira anytime...')
            a(href: "https://jira.url.com") {
              button(type: 'button', 'HDESK request')
            }
            em {
              p('The Software Development Team')
            }
          }
        }
      }
    }
  }
  return writer.toString()
}

//Function to add Jenkins clouds infos & templates
@NonCPS
def addCloud(xml, clouds, String name) {
  xml.with {
    div(class: 'entry-type') {
      h2(class: 'entry-type-title', "${name} template(s)")
      clouds.each{ cloudEntry ->
        cloudEntry.each{ cloud ->
          div(class: 'container') {
            p("Running on ${cloud.key} - hostname: ${cloud.value.hostname}")
            //templates
            cloud.value.templates.each{ template ->
              addNode(
                xml,
                [
                  ['Labels':template.labels],
                  ['Name':template.name],
                  ['Docker image':template.image],
                  ['Instance capacity':template.instanceCap],
                  ['Tools':template.toolsLocation],
                  ['Environment variables':template.envVars]
                ]
              )
            }
          }
        }
      }
    }
  }
}

// Function to add a table with nodes details
@NonCPS
def addNode(xml, details) {
  xml.with {
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
                    if (isSDP(image)){
                      String doc = getDocumentationURL(image)
                      td {
                        div(image)
                        div(class: 'image-documentation') { a(href: "${doc}", 'docker image documentation') }
                      }
                    } else {
                      td(image)
                    }
                    break
                  //tools
                  case 'Tools':
                    td(class: 'properties', "${detail.key}")
                    addTools(xml, detail.value)
                    break
                  //envVars
                  case 'Environment variables':
                    td(class: 'properties', "${detail.key}")
                    addEnvVars(xml, detail.value)
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
def addTools(xml, tools) {
  xml.with {
    td {
      tools.each { toolName, toolData -> //tool type
        b { p(class: 'tool-name', "${toolName}:") }
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
def addEnvVars(xml, envVars) {
    xml.with {
      td {
      table(class: 'details-table') {
        tr {
          th('Name')
          th('Value')
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

//Determine if a docker image is provided from SDP team
@NonCPS
Boolean isSDP(image) {
  String SDP_REGISTRY_ID = 'sf'
  String SDP_REGISTRY_SUFFIX = '-docker-registry'
  String SDP_REGISTRY = SDP_REGISTRY_ID+SDP_REGISTRY_SUFFIX
  return image.startsWith(SDP_REGISTRY) ? true : false
}

//Get URL documentation for a SDP docker image
@NonCPS
def getDocumentationURL(image) {
  def config = [
    'serverName':'sec-nexus-01',
    'domainName':'devops.in.idemia.com',
    'contextURL':'/nexus',
    'repositoryURL':'content/repositories',
    'repositoryID':'mph-nexus-01-mogl-release'
  ]
  try {
    String img = image.split('/', 2)[1]
    String group = img.substring(0, img.lastIndexOf('/'))
    String artifact = img.substring(img.lastIndexOf('/')+1, img.indexOf(':'))
    String version = img.split(':', 2)[1]
    String imageDocumentation = "https://${config.serverName}.${config.domainName}${config.contextURL}/${config.repositoryURL}/${config.repositoryID}/${group}/${artifact}/${version}/${artifact}-${version}.html"
    return imageDocumentation
  }
  catch(Exception ex) {
    return "https://${config.serverName}.${config.domainName}${config.contextURL}/${config.repositoryURL}/${config.repositoryID}/idemia/rds/sdp"
  }
}

//Function to return CSS style
@NonCPS
def getCSS() {
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
  a {
    text-decoration: none;
    color: black;
    border-bottom: 3px solid #0074d9;
  }
  a:hover {
    color: black;
    background-color: #0074d9;
  }
  a:visited {
    color: black;
  }
  a:active {
    color: #e74c3c;
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
