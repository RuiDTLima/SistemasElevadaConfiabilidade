
so=`uname -a`
mac=`echo $so | grep -c Darwin`
lin=`echo $so | grep -c Linux`

if [ $mac = 1 ]
then 
	osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd notary && java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080 && clear\"" > /dev/null
	osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd alice && java -jar alice.jar 1 --server.port=8081 && clear\"" > /dev/null
	osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd bob && java -jar bob.jar 2 --server.port=8082 && clear\"" > /dev/null
else
	gnome-terminal -x bash -c "cd notary && java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080 && clear; exec bash"
	gnome-terminal -x bash -c "cd alice && java -jar alice.jar 1 --server.port=8081 && clear; exec bash"
	gnome-terminal -x bash -c "cd bob && java -jar bob.jar 2 --server.port=8082; exec bash"
fi
