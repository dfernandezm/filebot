@ECHO OFF
java -XX:+TieredCompilation -Dunixfs=false -DuseExtendedFileAttributes=false -DuseCreationDate=false -Djava.net.useSystemProxies=false -Dsun.net.client.defaultConnectTimeout=10000 -Dsun.net.client.defaultReadTimeout=60000 -Djna.nosys=true -Dapplication.deployment=portable -Dapplication.analytics=true "-Dapplication.dir=%~dp0." "-Duser.home=%~dp0."  "-Djava.io.tmpdir=%~dp0temp" "-Djna.library.path=%~dp0" "-Djava.library.path=%~dp0" -Dnet.sourceforge.filebot.AcoustID.fpcalc="%~dp0fpcalc.exe" -Djava.util.prefs.PreferencesFactory=net.sourceforge.tuned.prefs.FilePreferencesFactory "-Dnet.sourceforge.tuned.prefs.file=%~dp0prefs.properties" -jar "%~dp0FileBot.jar" %*