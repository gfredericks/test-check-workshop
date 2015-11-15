#!/bin/sh

# Script to run on a fresh ubuntu vm; should install everything
# necessary and startup the server on port 8080.

# May as well run as root I guess.

# To bootstrap:
# apt-get install -y wget && wget https://raw.githubusercontent.com/gfredericks/4clojure/test.check-generators/vm-bootstrap.sh && sh vm-bootstrap.sh

apt-get update && apt-get install -y openjdk-7-jre git

wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
chmod +x lein
mv lein /usr/local/bin/

export LEIN_ROOT=1
lein version
git clone -b test.check-generators https://github.com/gfredericks/4clojure.git
cd 4clojure
./load-data.sh
lein run
