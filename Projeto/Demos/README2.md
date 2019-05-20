Os clientes podem efetuar as seguintes operações:
- **getStateOfGood \<good-id>** em que o **\<good-id>** é o identificador único do good.
- **intentionToSell \<good-id>** em que o **\<good-id>** é o identificador único do good.
- **buyGood \<good-id> \<owner-id>** em que o **\<good-id>** é o identificador único do good e o **\<owner-id>** é o identificador único do dono do good representado por **\<good-id>**.
- **exit** em que termina a sessão de um utilizador.

# Setup

Para cada um dos casos de teste há uma pasta com o nome alice ou bob, em cada uma dessas pastas deve ser copiado o ficheiro jars\hds_client.jar, sendo que o ficheiro deve ser renomeado para o nome correspondente ao nome da pasta, por exemplo na pasta alice, deve ser copiado o jars\hds_client.jar e renomeado para alice.jar.
Para todos os casos de teste, há uma pasta com o nome notaries, e dentro dessa pasta existe pelo menos quatro pasta com os nome first, second, third and fourth e para cada uma dessas pastas deve ser copiado o jar jars\hds_notary-0.0.1-SNAPSHOT.jar.

A aplicação permite executar os notários a assinarem as suas mensagens com o cartão de cidadão português ou usando um par de chaves. Para usar o par de chaves terá de ser passada a flag "--withPTCC=false", caso não passe a flag, por omissão irá usar o cartão de cidadão. Todos os scripts de demos estão preparados para correr sem o cartão de cidadão português. Para correr com o cartão de cidadão português é necessário alterar todos os scripts tirando a flag "--withPTCC=false" ou alterar esta flag passando o valor true.

Durante a realização dos testes é necessário ter os portos **8074**, **8075**, **8076**, **8077**,  **8081**, **8082** vagos. Para executar os vários testes é necessário primeiro executar o ficheiro script.bat, da pasta correspondente ao teste, e de seguida, de acordo com o teste que se deseja executar devem ser seguidos os seguintes passos.

As chaves privadas dos clientes estão protegidas por uma password de forma a que se uma chave privada for _leaked_ não possa ser usada sem o conhecimento da password que a protege. Assim a password para a chave privada da Alice é _alice_ e a password para a chave privada do Bob é _bob_. Sempre que iniciar estes clientes será pedido ao utilizador para introduzir a password de forma a aplicação puder ser inicializar.

# Casos de Teste 

## Demo 1:
  - Testa a compra de um bem inexistente.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização deve ser introduzido o seguinte comando: "buyGood 55 2".
  - No fim espera-se receber como aviso na Alice "The good with id 55 does not exist.".

## Demo 2:
  - Testa a compra de um bem que não está à venda.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta Demo.
  - Na janela pertencente ao Bob, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 1 1".
  - No fim espera-se receber como mensagem no Bob "NO".

## Demo 3:
  - Testa a compra a um utilizador de um bem que não possui.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta Demo.
  - Na janela pertencente ao Bob, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 1".
  - No fim espera-se receber como mensagem no Bob "Good with id 3 does not belong to the seller.".

## Demo 4:
  - Testa a compra a um utilizador inexistente.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 4".
  - No fim espera-se receber como mensagem na Alice "The user with id 4 does not exist."

## Demo 5:
  - Testa a compra do seu próprio bem.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta Demo.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 1 1".
  - No fim espera-se receber como mensagem na Alice "The user cannot buy from itself."

## Demo 6:
  - Testa assinar pedidos no lado do cliente com chaves falsas, a Alice possui uma chave falsa e o notário deve detetar isso.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta FakeKeyAlice.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como mensagem na Alice "This message is not authentic."

## Demo 7:
  - Testa assinar resposta a pedidos com chaves falsas, a chave privada do Notary é falsa, usa um cartão diferente do original.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - Durante este teste, cada notário deve ter uma chave privada que não corresponda à chave pública no cliente.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta FakeKeyNotaries.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como mensagem na Alice "Did not received a valid response."

## Demo 8:
  - Testa assinar pedidos no lado do cliente com chaves falsas, a chave privada do Bob é falsa e o Notary e a Alice devem detetar isso.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta FakeKeyBob.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 2".
  - No fim espera-se receber como mensagem na Alice e no Bob "This message is not authentic."

## Demo 9:
  - Testa assinar resposta a pedidos com chaves falsas.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - Durante este teste, deve ser usado um cartão de cidadão diferente do usado para gerar a chave pública.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta FakeKeyNotaries2.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 2".
  - No fim espera-se receber como mensagem na Alice "There was no valid responses."

## Demo 10:
  - Testa o envio de um intentionToSell com o wts errado.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida deve ser executado o ficheiro script.bat, na pasta AdvancedState.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "intentionToSell 1".
  - No fim espera-se receber como mensagem na Alice "Yes".
  - Para testar este teste uma segunda vez é necessário, em todas as pastas de notários, copiar o conteúdo do ficheiro "state - Cópia.json" para o ficheiro state.json.

## Demo 11:
  - Testa o envio de um buyGood com o wts errado.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida deve ser executado o ficheiro script.bat, na pasta AdvancedState.
  - Depois na janela pertencente à Alice, assim que o spring terminar a sua inicialização introduzir o seguinte comando: "buyGood 3 2".
  - No fim espera-se receber como mensagem na Alice "Yes".
  - Para testar este teste uma segunda vez é necessário, em todas as pastas de notários, copiar o conteúdo do ficheiro "state - Cópia.json" para o ficheiro state.json.

## Demo 12:
  - Testa a situação de dois clientes em conluio tentarem vender o mesmo bem duas vezes.
  - Este teste não é possível de executar uma vez que as aplicações cliente e notário foram feitas usando Spring e este atende cada pedido
    numa Thread diferente. A chamada ao Controller respetivo é feita de forma síncrona. Isto quer dizer que não existe concorrência no
    método do Controller que irá ser executado. Desta forma o primeiro pedido a ser atendido será realizado e a compra será efetuada. Assim, o segundo pedido
    será atendido mas não irá produzir nenhuma alteração de estado. Nessa situação, o teste passa a ser equivalente ao teste dos Demos 2 e 3.

## Demo 13:
  - Testa a situação em que um dos notários tem o write timestamps mais alto que todos os outros notários.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta InconsistentState.
  - Na janela pertencente à Alice, executar o comando "getStateOfGood 1", esperar pelo resultado "The good with id 1 is owned by user with id 1 and his state is not-on-sale.".
  - Para verificar que os notários aceitaram o write timestamp que a Alice fez write back, deve aceder às pastas notaries/second, notaries/third, notaries/fourth e abrir o ficheiro state.json. Nesse ficheiro deve ser verificado que o wts do good com o id 1 tem o valor 7.
  - Para testar este teste uma segunda vez é necessário, em todas as pastas de notários, copiar o conteúdo do ficheiro "state - Cópia.json" para o ficheiro state.json.

## Demo 14:
  - Testa a situação em que um dos quatros notários é bizantino. Para simular essa situação, durante este teste um dos notários de que os clientes estão à espera não estará ativo.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta OneNotaryByzantine.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização deve ser introduzido o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como resposta na Alice "The good with id 1 is owned by user with id 1 and his state is not-on-sale.".

## Demo 15:
  - Testa a situação em que dois dos quatro notários são bizantinos. Para simular essa situação, durante este teste dois dos notários de que os clientes estão à espera não estarão ativos.
  - Para este teste é necessário terminar todas as janelas de comandos, correspondentes aos clientes Alice, Bob e aos Notaries.
  - De seguida, deve ser executado o ficheiro script.bat, na pasta TwoNotariesByzantine.
  - Na janela pertencente à Alice, assim que o spring terminar a sua inicialização deve ser introduzido o seguinte comando: "getStateOfGood 1".
  - No fim espera-se receber como resposta na Alice "Did not received a valid response.".