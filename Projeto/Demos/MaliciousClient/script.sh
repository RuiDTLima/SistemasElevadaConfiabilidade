
so=`uname -a`
mac=`echo $so | grep -c Darwin`
lin=`echo $so | grep -c Linux`

if [ $mac = 1 ]
then 
	osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd notary && java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080 && clear\"" > /dev/null
	osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd eve && java -jar eve.jar 3 --server.port=8083 && clear\"" > /dev/null
else
	gnome-terminal -x bash -c "cd notary && java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080 && clear; exec bash"
	gnome-terminal -x bash -c "cd eve && java -jar eve.jar 3 --server.port=8083; exec bash"
fi
