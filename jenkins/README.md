# Jenkins Pipeline Script Documentation

This documentation explains how this script generates an HTML report containing information about Jenkins nodes, Docker cloud templates, and Kubernetes cloud templates. The script is written in Groovy and is designed to run as a Jenkins pipeline job.

## Overview

The final report is supposed to help Jenkins users to understand what tools is inside their Jenkins instance in order to organize their jobs & pipelines.

The script collects data about Jenkins nodes and cloud configurations and presents it in a structured HTML report format.
There is two main step:
1. Get data from Jenkins instance - `getJenkinsNodes()`
2. Diplay results inside an HTML report built using MarkupBuilder (groovy librairy) - `buildReport()`



## Get Jenkins nodes and templates

The `getJenkinsNodes()` function is responsible to parse all Jenkins slaves (Jenkins.instance.nodes) and get some informations like:
- Node name
- Node labels
- Launcher type : SSH or JNLP
- Node operating system
- Hostname
- Tool locations
- Environment variables

but also Cloud (Docker and Kubernetes) templates and get associates data, such as:
- Template name
- Template labels
- Docker image used
- Tool locations
- Environment variables

The `getJenkinsNodes()` will return a Groovy map output that looks like : 

// insert code
[
  nodes:[
    [
      labels:jnlp1 win10 windows,
      name:Windows,
      launcher:JNLP,
      hostname:build-win10,
      instanceCap:2,
      os:null,
      status:Offline,
      envVars:null,
      toolsLocation:null
    ],
    [
      labels:label1 label2 label3,
      name:node-SSH,
      launcher:SSH,
      hostname:10.126.5.3,
      instanceCap:4,
      os:null,
      status:Offline,
      envVars:[
        PATH+GIT_HOME:/opt/git/bin/git,
        PATH+JAVA_HOME:/opt/java/bin
      ],
      toolsLocation:[
        Git:[
          [
            home:/opt/git,
            name:Default
          ]
        ]
      ]
    ]
  ],
  dockerCloud:[
    [
      rmi-dockerhost:[
        hostname:frosssfrm1v1011.devops.in.idemia.com,
        templates:[
          [
            labels:label,
            name:name,
            image:sf-registry/alpine:10.2,
            instanceCap:4,
            disabled:true,
            envVars:null,
            toolsLocation:null
          ],
          [
            labels:linux,
            name:linux,
            image:registry.docker/linux:1.2,
            instanceCap:2,
            disabled:true,
            envVars:null,
            toolsLocation:null
          ],
          [
            labels:ubuntu linux testing,
            name:ubuntu20.04,
            image:sf-docker-registry.devops.in.idemia.com/idemia/ubuntu-std:2023-01,
            instanceCap:4,
            disabled:false,
            envVars:null,
            toolsLocation:null
          ]
        ]
      ]
    ]
  ],
  kubernetesCloud:[
    [
      kubernetes:[
        hostname:frosssfks1v1001.devops.in.idemia.com,
        templates:[]
      ]
    ]
  ]
]


## Building report

The HTML Report is built thanks to `buildReport()` taking with an map argument. From input argument, the function is generated using Groovy `MarkupBuilder`, and it display informations using several sub-functions.


## Publish report

This final HTML report is published in "${JENKINS_HOME}/userContent/nodes.html" inside userContent.

## Author

- Sebastien LESOURD