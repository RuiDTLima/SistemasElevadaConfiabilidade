osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd notary && java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080 && clear\"" > /dev/null
osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd alice && java -jar alice.jar 1 --server.port=8081 && clear\"" > /dev/null
osascript -e "tell application \"Terminal\" to do script \"cd $PWD && cd bob && java -jar bob.jar 2 --server.port=8082 && clear\"" > /dev/null