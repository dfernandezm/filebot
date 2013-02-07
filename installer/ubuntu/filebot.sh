#!/bin/bash
java -Xmx256m -Dunixfs=false -DuseGVFS=true -DuseExtendedFileAttributes=true -Djava.net.useSystemProxies=true -Dsun.net.client.defaultConnectTimeout=10000 -Dsun.net.client.defaultReadTimeout=60000 -Dapplication.update=skip -Dapplication.deployment=ppa -Dapplication.analytics=false -Dapplication.warmup=true -Dapplication.dir=$HOME/.filebot -Djava.io.tmpdir=$HOME/.filebot/temp -Djna.library.path=/opt/filebot -Djava.library.path=/opt/filebot -jar /opt/filebot/FileBot.jar "$@"