ECHO OFF
cd alice
start cmd /k java -jar alice.jar 1 1 --server.port=8081
cd ../bob
start cmd /k java -jar bob.jar 2 1 --server.port=8082
cd ../notaries/first
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 0 1 --server.port=8074 --withPTCC=false
cd ../second
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 1 1 --server.port=8075 --withPTCC=false
cd ../third
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 2 1 --server.port=8076 --withPTCC=false
cd ../fourth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar 3 1 --server.port=8077 --withPTCC=false