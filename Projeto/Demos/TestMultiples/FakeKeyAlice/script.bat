ECHO OFF
cd alice
start cmd /k java -jar alice.jar 1 1 --server.port=8081
cd ../notaries/first
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8074
cd ../second
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8075
cd ../third
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8076
cd ../fourth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8077