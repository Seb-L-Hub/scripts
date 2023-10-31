# Function declaration
setupTechuser() {
  # Variable initialization
  declare PROJECT_NAME=""
  declare INSTANCE_NAME=""
  declare INSTANCE_PREFIX="${INSTANCE_PREFIX:-jenkins-}"
 
  # Setup project name from command line or user input if parameter missing
  if [[ $# -lt 1 ]]; then
    read -p "Enter project name:
${INSTANCE_PREFIX}" PROJECT_NAME
  else
    PROJECT_NAME="${1}"
  fi
   
  # Check project name
  PROJECT_NAME="${PROJECT_NAME##*${INSTANCE_PREFIX}}"
  [[ -z ${PROJECT_NAME} ]] && { echo "Missing instance name" >&2; return 1; }
 
 
  #
  # User setup
  INSTANCE_NAME="${INSTANCE_PREFIX}${PROJECT_NAME}"
  sudo adduser --system --ingroup docker --home "/data/${INSTANCE_NAME}" --shell /bin/bash "${INSTANCE_NAME}"
  sudo passwd "${INSTANCE_NAME}"
  #Plugins spotify do not need to know the certificates
  #sudo -iu "${INSTANCE_NAME}" mkdir -p .docker/
  #sudo -iu "${INSTANCE_NAME}" ln -s /etc/docker/{cert,key}.pem .docker/
  #sudo -iu "${INSTANCE_NAME}" ln -s /etc/ssl/certs/idemia-root-ca.pem .docker/ca.pem
 
  # update /etc/login.group.allowed if needed
  if [[ -f /etc/login.group.allowed  ]]; then
    if [[ $(sudo grep -c docker /etc/login.group.allowed) -eq 0 ]]; then
      sudo bash -c "echo docker >> /etc/login.group.allowed"
    fi
  fi
}
 
# Call function
setupTechuser



#OLD to check
sudo useradd -b /data -g docker -s /bin/bash -m jenkins-<jira-key-lowercase>
sudo adduser --system --group --ingroup docker --home "/data/jenkins-<jira-key-lowercase>" --shell /bin/bash "jenkins-<jira-key-lowercase>"
sudo passwd jenkins-<jira-key-lowercase>


# Create .docker directory if missing for user jenkins-<jira-key-lowercase>
$ sudo -iu jenkins-<jira-key-lowercase> mkdir -p .docker/
# Copy certificates to .docker for user jenkins-<jira-key-lowercase>
$ sudo -iu jenkins-<jira-key-lowercase> ln -s /etc/docker/{ca,cert,key}.pem .docker/
