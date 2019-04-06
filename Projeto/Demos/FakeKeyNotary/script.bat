ECHO OFF
cd notary
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080
cd ../alice
start cmd /k java -jar alice.jar 1 --server.port=8081