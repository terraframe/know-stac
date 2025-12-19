#
# Copyright 2025 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This tells the build which version of npm to use:
. $NVM_DIR/nvm.sh && nvm install lts/jod

:
: ----------------------------------
:  CONFIGURE  
: ----------------------------------
:

export MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.resolver.transport=wagon"
export ANSIBLE_HOST_KEY_CHECKING=false
export NODE_OPTIONS="--max_old_space_size=1500"

export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120

:
: ----------------------------------
:  BUILD
: ----------------------------------
:

if [ "$build_artifact" = "true" ]; then
  cd "$WORKSPACE/knowstac"

  if [ "$environment" = "dev" ]; then
    mvn clean install -B -Pdev
  else
    mvn clean install -B
  fi
fi

:
: ----------------------------------
:  DEPLOY
: ----------------------------------
:

if [ "$deploy" == "true" ]; then
  # Build a Docker image
  cd $WORKSPACE/knowstac/src/build/docker/know-stac/api-web && ./build.sh
  cd $WORKSPACE/knowstac/src/build/docker/know-stac/ui-web && ./build.sh

  # Run Ansible deploy
  cd $WORKSPACE/geoprism-cloud/ansible

  [ -h ./inventory ] && unlink ./inventory
  [ -d ./inventory ] && rm -r ./inventory
  ln -s $WORKSPACE/geoprism-platform/ansible/inventory ./inventory

  [ -h ../permissions ] && unlink ../permissions
  ln -s $WORKSPACE/geoprism-platform/permissions ../permissions

  ansible-playbook -v -i ./inventory/knowstac/$environment.ini ./knowstac.yml --extra-vars "clean_db=$clean_db clean_solr=$clean_solr clean_orientdb=$clean_orientdb elasticsearch_clean=$elasticsearch_clean webserver_docker_image_tag=$tag docker_image_path2=../../knowstac/src/build/docker/know-stac/api-web/target/knowstac.dimg.gz docker_image_path=../../knowstac/src/build/docker/know-stac/ui-web/target/knowstac-ui.dimg.gz"
fi
