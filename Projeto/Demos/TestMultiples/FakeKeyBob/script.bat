ECHO OFF
cd alice
start cmd /k java -jar alice.jar 1 1 --server.port=8081
cd ../bob
start cmd /k java -jar bob.jar 2 1 --server.port=8082
cd ../notaries/first
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8074
cd ../notaries/second
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8075
cd ../notaries/third
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8076
cd ../notaries/fourth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8077