ECHO OFF
cd alice
start cmd /k java -jar alice.jar 1 1 --server.port=8081
cd ../notaries/first
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 0 1 --server.port=8074
cd ../second
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 1 1 --server.port=8075