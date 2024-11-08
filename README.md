Projeto - Suprimentos
Módulo java referente a duas ações no Saknhya ERP, sendo um botão de ação na TGFCAB - Pedido de Compras, e outro na tela de Desp. Financeira Recorrente.
O primeiro botão é responsável por inserir um registro na tela de Desp. Financeira Recorrente, com alguns dados importantes, tais como; Número da nota, Parceiro, Data do movimento, etc. Além de fazer esse insert, a ação também deleta o financeiro gerado pela TOP de pedido.

O segundo botão de ação atua na tela de Desp. Financeira Recorrente, essa ação é responsável por realizar a inserção de um novo financeiro referente ao pedido, esse financeiro entra na TGFFIN como uma provisão de despesa, gerando o NUFIN.

Essas ações possuem algumas travas, tais como: no passo 1 só é levado os dados da nota para a tela de Desp. Financeira Recorrente, se houver a confirmação do gestor da área, caso contrário o pedido não gera registro na na tabela.

Só é possível gerar 1 financeiro na tabela TGFFIN, caso já exista um NUFIN para esse pedido o sistema irá barrar uma nova inserção.


InsereTelaDesp.java - Atua no portal de compras, gerando um registro na tela de Desp. Financeira Recorrente.
InsereFin.java - Atua na tela de Desp.Financeira Recorrente, gerando um registro no financeiro.
