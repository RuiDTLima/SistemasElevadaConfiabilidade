ECHO OFF
cd first
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8074
cd ../second
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8075
cd ../third
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8076
cd ../fourth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8077
cd ../fifth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8078
cd ../sixth
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8079
cd ../seventh
start cmd /k java -jar hds_notary-0.0.1-SNAPSHOT.jar --server.port=8080
