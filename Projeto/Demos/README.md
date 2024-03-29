Os clientes podem efetuar as seguintes operações:
- **getStateOfGood \<good-id>** em que o **\<good-id>** é o identificador único do good.
- **intentionToSell \<good-id>** em que o **\<good-id>** é o identificador único do good.
- **buyGood \<good-id> \<owner-id>** em que o **\<good-id>** é o identificador único do good e o **\<owner-id>** é o identificador único do dono do good representado por **\<good-id>**.

# Setup

Para cada um dos casos de teste há uma pasta com o nome alice ou bob, em cada uma dessas pastas deve ser copiado o ficheiro jars\hds_client.jar, sendo que o ficheiro deve ser renomeado para o nome correspondente ao nome da pasta, por exemplo na pasta alice, deve ser copiado o jars\hds_client.jar e renomeado para alice.jar.
Para os casos de teste que tenham uma pasta chamada eve, deve ser copiado para essas pastas o jar jars\hds_client_malicious.jar e renomeado para eve.jar.
Para todos os casos de teste, há uma pasta com o nome notary, e para cada uma dessas pastas deve ser copiado o jar jars\hds_notary-0.0.1-SNAPSHOT.jar.

Para a realização dos testes é necessário guardar a chave pública correspondente ao cartão de cidadão a usar durante os testes, no ficheiro
notary.pub. Esse ficheiro deve ser colocado nos seguintes diretórios:
  - Demo\alice\keys
  - Demo\bob\keys
  - FakeKeyNotary\alice\keys
  - FakeKeyAlice\alice\keys 
  - FakeKeyBob\alice\keys
  - FakeKeyBob\bob\keys
  - FakeKeyNotary2\alice\keys
  - FakeKeyNotary2\bob\keys
  - MaliciousClient\eve\keys
  - MaliciousClient2\alice\keys
  - MaliciousClient2\eve\keys. 

Durante a realização dos testes é necessário ter os portos **8080**, **8081**, **8082** e **8083** vagos. Para executar os vários testes é necessário primeiro executar o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja
usado MacOS ou Ubuntu, da pasta correspondente ao teste, e de seguida, de acordo com o teste que se deseja executar devem ser seguidos os seguintes passos.

# Casos de Teste 

## Demo 1:
  - Testa a compra de um bem inexistente.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 55 2".
  - No fim espera-se receber como aviso na Alice "The good with id 55 does not exist.".

## Demo 2:
  - Testa a compra de um bem que não está à venda.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta Demo.
  - Na janela pertencente ao Bob, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 1 1".
  - No fim espera-se receber como mensagem no Bob "NO".

## Demo 3:
  - Testa a compra a um utilizador de um bem que não possui.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta Demo.
  - Na janela pertencente ao Bob, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 1".
  - No fim espera-se receber como mensagem no Bob "Good with id 3 does not belong to the seller.".

## Demo 4:
  - Testa a compra a um utilizador inexistente.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 4".
  - No fim espera-se receber como mensagem na Alice "The user with id 4 does not exist."

## Demo 5:
  - Testa a compra do seu próprio bem.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 1 1".
  - No fim espera-se receber como mensagem na Alice "The user cannot buy from itself."

## Demo 6:
  - Testa assinar pedidos no lado do cliente com chaves falsas, a Alice possui uma chave falsa e o notário deve detetar isso.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta FakeKeyAlice.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como mensagem na Alice "This message is not authentic."
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes à cliente Alice e ao Notary devem ser encerradas.

## Demo 7:
  - Testa assinar resposta a pedidos com chaves falsas, a chave privada do Notary é falsa, usa um cartão diferente do original.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - Durante este teste, deve ser usado um cartão de cidadão diferente do usado para gerar a chave pública.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta FakeKeyNotary.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como mensagem na Alice "Could not verify the message"
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes à cliente Alice e ao Notary devem ser encerradas.

## Demo 8:
  - Testa assinar pedidos no lado do cliente com chaves falsas, a chave privada do Bob é falsa e o Notary e a Alice devem detetar isso.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta FakeKeyBob.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 2".
  - No fim espera-se receber como mensagem na Alice e no Bob "This message is not authentic."
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes aos clientes Alice e Bob e ao Notary devem ser encerradas.

## Demo 9:
  - Testa assinar resposta a pedidos com chaves falsas.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - Durante este teste, deve ser usado um cartão de cidadão diferente do usado para gerar a chave pública.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta FakeKeyNotary2.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 2".
  - No fim espera-se receber como mensagem na Alice "Could not verify the message."
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes aos clientes Alice e Bob e ao Notary devem ser encerradas.
  - Para testar este teste uma segunda vez é necessário copiar o conteúdo do ficheiro state_copy.json para o ficheiro state.json.

## Demo 10:
  - Testa o envio por parte de um cliente malicioso de mensagens repetidas.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta MaliciousClient.
  - Depois na janela pertencente à Eve, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "getStateOfGood 1".
  - Assim que receber a resposta a este pedido, deve ser introduzido o seguinte comando na janela pertencente à Eve: "sendLastMessage".
  - No fim espera-se receber como mensagem na Eve "The message received is out of time, it was sent before the last one.".
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes à cliente Eve e ao Notary devem ser encerradas.

## Demo 11:
  - Testa o envio por parte de um cliente malicioso de mensagens repetidas.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e Eve e ao Notary.
  - De seguida, deve ser executado o ficheiro script.bat, caso seja usado Windows, ou o ficheiro script.sh, caso seja usado MacOS ou Ubuntu, na pasta MaliciousClient2.
  - Depois na janela pertencente à Eve, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 1 1".
  - Assim que receber a resposta a este pedido, deve ser introduzido o seguinte comando na janela pertencente à Eve: "intentionToSell 1".
  - De seguida, deve ser introduzido o seguinte comando na janela pertencente à Alice: "buyGood 1 3".
  - Assim que receber a resposta a este pedido, deve ser introduzido o seguinte comando na janela pertencente à Alice: "intentionToSell 1".
  - De seguida, deve ser introduzido o seguinte comando na janela pertencente à Eve: "sendLastMessage".
  - No fim espera-se receber como mensagem na Eve "The message received is out of time, it was sent before the last one.".
  - Finalmente, após a execução do teste, todas as janela de comandos, correspondentes à cliente Alice e Eve e ao Notary devem ser encerradas.
  - Para testar este teste uma segunda vez é necessário copiar o conteúdo do ficheiro state_copy.json para o ficheiro state.json.

## Demo 12:
  - Testa a situação de dois clientes em conluio tentarem vender o mesmo bem duas vezes.
  - Este teste não é possível de executar uma vez que as aplicações cliente e notário foram feitas usando Spring e este atende cada pedido
    numa Thread diferente. A chamada ao Controller respetivo é feita de forma síncrona. Isto quer dizer que não existe concorrência no
    método do Controller que irá ser executado. Desta forma o primeiro pedido a ser atendido será realizado e a compra será efetuada. Assim, o segundo pedido
    será atendido mas não irá produzir nenhuma alteração de estado. Nessa situação, o teste passa a ser equivalente ao teste dos Demos 2 e 3.
