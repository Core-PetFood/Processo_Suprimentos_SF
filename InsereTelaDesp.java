package br.com.sankhya.SUPERFOOD.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.utils.nome.TabelaNome;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
public class InsereTelaDesp implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();

        BigDecimal proxPK = null;
        BigDecimal vlrTot = null;

        try {
            // Insere Tabela PAI
            NativeSql incrementaPKPai = new NativeSql(jdbc);
            incrementaPKPai.appendSql("SELECT MAX(NUFINREC) + 1 AS PROX_NUFINREC FROM AD_TGFDESPFIN");
            ResultSet rs = incrementaPKPai.executeQuery();
            if (rs.next()) {
                proxPK = rs.getBigDecimal("PROX_NUFINREC");
            }
            rs.close(); // Fechando o ResultSet para liberar o recurso

            JapeWrapper despesaFinDAO = JapeFactory.dao(TabelaNome.AD_TGFDESPFIN);
            FluidCreateVO fluidCreateVOPai = despesaFinDAO.create();
            fluidCreateVOPai.set("NUFINREC", proxPK);
            fluidCreateVOPai.set("DESCRDESP", "Despesa");
            fluidCreateVOPai.save();

            // Insere Tabela Filha
            Registro reg = contexto.getLinhas()[0];
            BigDecimal codemp = (BigDecimal) reg.getCampo("CODEMP");
            BigDecimal numnota = (BigDecimal) reg.getCampo("NUMNOTA");
            BigDecimal nunota = (BigDecimal) reg.getCampo("NUNOTA");
            Timestamp dtneg = (Timestamp) reg.getCampo("DTNEG");
            BigDecimal codParc = (BigDecimal) reg.getCampo("CODPARC");
            BigDecimal codtipoper = (BigDecimal) reg.getCampo("CODTIPOPER");
            BigDecimal codNat = (BigDecimal) reg.getCampo("CODNAT");
            BigDecimal codcencus = (BigDecimal) reg.getCampo("CODCENCUS");
            BigDecimal codusu = (BigDecimal) reg.getCampo("CODUSU");
            BigDecimal codvende = (BigDecimal) reg.getCampo("CODVEND");
            Timestamp dtentsai = (Timestamp) reg.getCampo("DTENTSAI");
            String observacao = (String) reg.getCampo("OBSERVACAO");

            // Busca o VLRTOT ITE
            NativeSql buscaVlrTot = new NativeSql(jdbc);
            buscaVlrTot.appendSql("SELECT VLRTOT FROM TGFITE WHERE NUNOTA = :NUNOTA");
            buscaVlrTot.setNamedParameter("NUNOTA", nunota);
            ResultSet rs2 = buscaVlrTot.executeQuery();
            if (rs2.next()) {
                vlrTot = rs2.getBigDecimal("VLRTOT");
            }
            rs2.close();

            // Exclui registros financeiros relacionados à nota
            NativeSql excluiFinNota = new NativeSql(jdbc);
            excluiFinNota.appendSql("DELETE FROM TGFFIN WHERE NUNOTA = :NUNOTA");
            excluiFinNota.setNamedParameter("NUNOTA", nunota);
            excluiFinNota.executeUpdate();

            // Verifica se o pedido de compra foi aprovado
            NativeSql verificaStatusPedido = new NativeSql(jdbc);
            verificaStatusPedido.appendSql("SELECT VLRLIBERADO FROM TSILIB WHERE NUCHAVE = :NUNOTA AND TABELA = 'TGFCAB' AND EVENTO = 44");
            verificaStatusPedido.setNamedParameter("NUNOTA", nunota);
            ResultSet rs5 = verificaStatusPedido.executeQuery();
            if (rs5.next() && rs5.getBigDecimal("VLRLIBERADO").compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("Pedido de compra não aprovado, gentileza aguardar a liberação do gestor.");
            }
            rs5.close();

            // Insere registro na tabela filha
            JapeWrapper despFinDAO = JapeFactory.dao(TabelaNome.AD_DESPFIN);
            FluidCreateVO fluidCreateVO = despFinDAO.create();
            fluidCreateVO.set("NUFINREC", proxPK); // Referência para tabela PAI (Foreign Key)
            fluidCreateVO.set("NUDESP", proxPK);   // PK da tabela filha
            fluidCreateVO.set("NUFIN", null);
            fluidCreateVO.set("CODEMP", codemp);
            fluidCreateVO.set("NUMNOTA", numnota);
            fluidCreateVO.set("NUNOTA", nunota);
            fluidCreateVO.set("SERIENOTA", BigDecimal.ZERO);
            fluidCreateVO.set("DTNEG", dtneg);
            fluidCreateVO.set("CODPARC", codParc);
            fluidCreateVO.set("CODTIPOPER", codtipoper);
            fluidCreateVO.set("CODNAT", codNat);
            fluidCreateVO.set("CODCENCUS", codcencus);
            fluidCreateVO.set("CODUSU", codusu);
            fluidCreateVO.set("CODVEND", codvende);
            fluidCreateVO.set("DTENTSAI", dtentsai);
            fluidCreateVO.set("HISTDESP", observacao);
            fluidCreateVO.set("VLRDESP", vlrTot);
            fluidCreateVO.save();

            contexto.setMensagemRetorno("Inserção realizada com sucesso, Registro: " + proxPK);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar o registro: " + e.getMessage());
        } finally {
            jdbc.closeSession();
        }
    }
}
