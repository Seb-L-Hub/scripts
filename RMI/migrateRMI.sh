#source

#!/bin/bash
sudo -i

setup_tools() {
  target_dir='/data/tmp/opt/'
  tools_dir='/opt/'
  # remove existing content
  if [ -d "$target_dir" ]; then
    rm -rf "$target_dir"
  fi
  #create target directory
  mkdir -p "$target_dir"
  chmod -R 777 "$target_dir"
  #save data
  rsync -av "$tools_dir" "$target_dir"
}
setup_tools

#target

#!/bin/bash
install_tools() {
  username=$USER
  read -p "Enter target RMI (hostname/IP): " srv
  # define the source and destination directories
  srv_dir="/data/tmp/opt/"
  target_dir="/opt/"
  #sync data
  sudo rsync -av "$username@$srv:$srv_dir" "$target_dir"
}
install_tools