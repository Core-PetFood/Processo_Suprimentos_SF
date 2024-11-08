package br.com.sankhya.SUPERFOOD.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class InsereFin implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        JdbcWrapper jdbc = null;
        jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        jdbc.openSession();
        BigDecimal proxPK = null;
        Timestamp maxDhAlter = null;

        NativeSql incrementaPKPai = new NativeSql(jdbc);
        incrementaPKPai.appendSql("SELECT MAX(NUFIN) + 1 AS PROX_NUFIN FROM TGFFIN");
        ResultSet rs = incrementaPKPai.executeQuery();

        while (rs.next()) {
            proxPK = rs.getBigDecimal("PROX_NUFIN");
        }
        rs.close();

        try {

            Registro reg = contexto.getLinhas()[0];
            BigDecimal codemp = (BigDecimal) reg.getCampo("CODEMP");
            BigDecimal numnota = (BigDecimal) reg.getCampo("NUMNOTA");
            BigDecimal nunota = (BigDecimal) reg.getCampo("NUNOTA");
            Timestamp dtneg = (Timestamp) reg.getCampo("DTNEG");
            BigDecimal codParc = (BigDecimal) reg.getCampo("CODPARC");
            BigDecimal codtipoper = (BigDecimal) reg.getCampo("CODTIPOPER");
            BigDecimal codNat = (BigDecimal) reg.getCampo("CODNAT");
            BigDecimal codcencus = (BigDecimal) reg.getCampo("CODCENCUS");
            BigDecimal codvende = (BigDecimal) reg.getCampo("CODVEND");
            Timestamp dtentsai = (Timestamp) reg.getCampo("DTENTSAI");
            BigDecimal vlrTot = (BigDecimal) reg.getCampo("VLRDESP");
            BigDecimal codtiptit = (BigDecimal) reg.getCampo("CODTIPTIT");
            String historico = (String) reg.getCampo("HISTDESP");
            BigDecimal pkPai = (BigDecimal) reg.getCampo("NUFINREC");
            BigDecimal pkFilha = (BigDecimal) reg.getCampo("NUDESP");

            // Busca a última data de alteração para o tipo de operação
            NativeSql buscaDhAlterTop = new NativeSql(jdbc);
            buscaDhAlterTop.appendSql("SELECT MAX(DHALTER) AS MAX_DHALTER FROM TGFTOP WHERE CODTIPOPER = :CODTIPOPER");
            buscaDhAlterTop.setNamedParameter("CODTIPOPER", codtipoper);
            ResultSet rs2 = buscaDhAlterTop.executeQuery();

            while (rs2.next()) {
                maxDhAlter = rs2.getTimestamp("MAX_DHALTER");
            }
            rs2.close();

            // Verifica se o pedido de compra foi aprovado
            boolean isAprovado = false;
            NativeSql verificaStatusPedido = new NativeSql(jdbc);
            verificaStatusPedido.appendSql("SELECT VLRLIBERADO FROM TSILIB WHERE NUCHAVE = :NUNOTA AND TABELA = 'TGFCAB' AND EVENTO = 44");
            verificaStatusPedido.setNamedParameter("NUNOTA", nunota);
            ResultSet rs5 = verificaStatusPedido.executeQuery();

            while (rs5.next()) {
                BigDecimal vlrliberado = rs5.getBigDecimal("VLRLIBERADO");
                if (vlrliberado.compareTo(BigDecimal.ZERO) > 0) {
                    isAprovado = true;
                } else {
                    throw new Exception("Pedido de compra não aprovado, gentileza aguardar a liberação do gestor.");
                }
            }
            rs5.close();

            // Bloqueia duplicidade de registros no financeiro
            NativeSql verificaFinanceiroCriado = new NativeSql(jdbc);
            verificaFinanceiroCriado.appendSql("SELECT NUFIN FROM AD_DESPFIN WHERE NUNOTA = :NUNOTA AND NUDESP = :PKFILHA");
            verificaFinanceiroCriado.setNamedParameter("NUNOTA", nunota);
            verificaFinanceiroCriado.setNamedParameter("PKFILHA", pkFilha);
            ResultSet rs7 = verificaFinanceiroCriado.executeQuery();

            // Se já existe um registro com o mesmo NUFIN, lançamos a exceção
            if (rs7.next()) {
                BigDecimal nufin = rs7.getBigDecimal("NUFIN");
                if(nufin != null) {
                    throw new Exception("Já existe uma movimentação Financeira para essa nota.");
                }
            }
            rs7.close();


            if (isAprovado) {
                try (CallableStatement callableStatement = jdbc.getConnection().prepareCall("{CALL INSERE_FIN(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                    callableStatement.setBigDecimal(1, proxPK); // P_NUFIN
                    callableStatement.setBigDecimal(2, codemp); // P_CODEMP
                    callableStatement.setBigDecimal(3, numnota); // P_NUMNOTA
                    callableStatement.setBigDecimal(4, nunota); // P_NUNOTA
                    callableStatement.setString(5, "1"); // P_SERIENOTA
                    callableStatement.setDate(6, new java.sql.Date(dtneg.getTime())); // P_DTNEG
                    callableStatement.setInt(7, 1); // P_DESDOBRAMENTO
                    callableStatement.setBigDecimal(8, BigDecimal.ZERO); // P_PRAZO
                    callableStatement.setBigDecimal(9, codParc); // P_CODPARC
                    callableStatement.setBigDecimal(10, codtipoper); // P_CODTIPOPER
                    callableStatement.setDate(11, new java.sql.Date(maxDhAlter.getTime())); // P_DHTIPOPER
                    callableStatement.setBigDecimal(12, codNat); // P_CODNAT
                    callableStatement.setBigDecimal(13, codcencus); // P_CODCENCUS
                    callableStatement.setInt(14, 0); // P_CODPROJ
                    callableStatement.setBigDecimal(15, codvende); // P_CODVEND
                    callableStatement.setBigDecimal(16, codtiptit); // P_CODTIPTIT
                    callableStatement.setBigDecimal(17, vlrTot); // P_VLRPARCELA
                    callableStatement.setDate(18, new java.sql.Date(dtentsai.getTime())); // P_DTENTSAI
                    callableStatement.execute();

                    contexto.setMensagemRetorno("Registro inserido com sucesso: " + proxPK);
                }

                // Atualiza histórico e núm. de documentos relacionados
                NativeSql atualizaHistorico = new NativeSql(jdbc);
                atualizaHistorico.appendSql("UPDATE TGFFIN SET HISTORICO = :HISTORICO WHERE NUFIN = :NUFIN");
                atualizaHistorico.setNamedParameter("HISTORICO", historico);
                atualizaHistorico.setNamedParameter("NUFIN", proxPK);
                atualizaHistorico.executeUpdate();

                NativeSql atualizaNufin = new NativeSql(jdbc);
                atualizaNufin.appendSql("UPDATE AD_DESPFIN SET NUFIN = :PROXPK WHERE NUFINREC = :NUFINREC AND NUDESP = :NUDESP");
                atualizaNufin.setNamedParameter("PROXPK", proxPK);
                atualizaNufin.setNamedParameter("NUFINREC", pkPai);
                atualizaNufin.setNamedParameter("NUDESP", pkFilha);
                atualizaNufin.executeUpdate();
            }

        } catch (Exception e) {
            throw new Exception("Erro ao inserir registro: " + e.getMessage(), e);
        }
    }
}
