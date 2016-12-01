package controle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import Conexao.ZooKeeperConnection;
import SyncPrimitive.BarrierQueueLock.Barrier;
import bean.Cliente;
import bean.Conta;
import bean.Transacao;

public class ControleTransacao {
	private ZooKeeper zk;

	private ZooKeeperConnection conexao;

	private void create(String path, byte[] data) throws KeeperException,
			InterruptedException {
		zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
	}

	public Transacao criarTransacao(Integer codigo, String descricao,
			Integer operacao, Double valor, Cliente cliente, String data) {
		Transacao t = new Transacao(codigo, descricao, operacao, valor,
				cliente, data);
		znodeTransacao(cliente, t);
		return t;
	}

	public void znodeTransacao(Cliente c, Transacao t) {

		String caminho = "/Clientes/" + c.getCpf() + "/transacoes";
		String dados = t.getOperacao() + "," + t.getValor();

		try {
			conexao = new ZooKeeperConnection();
			zk = conexao.connect("localhost");

			create(caminho, dados.getBytes()); 
			conexao.close();
			System.out.println("Transação adicionada com sucesso");
		} catch (Exception e) {
			System.out.println(e.getMessage()); 
		}
	}

	//Adicionei o throw do InputMismatchException 
	@SuppressWarnings("static-access")
	public void processarTransacao(Transacao t, String leaderPath) throws InputMismatchException, KeeperException, InterruptedException, IOException {
		try {
			Stat stat = null;
			conexao = new ZooKeeperConnection();
			zk = conexao.connect("localhost");

			String tipoOperacao = null;
			System.out.println(t.getCliente().getCpf());

			String caminho = leaderPath + "/Clientes/" + t.getCliente().getCpf() + "/conta";
			byte[] b = zk.getData(caminho, false, stat);

			String dados = new String(b, "UTF-8");
			String[] dadosConta = dados.split(",");
			int agencia = Integer.parseInt(dadosConta[0]);
			int numero = Integer.parseInt(dadosConta[1]);
			double saldo = Double.parseDouble(dadosConta[2]);
			double limite = Double.parseDouble(dadosConta[3]);

			switch (t.getOperacao()) {
			case 1:
				tipoOperacao = "Saque";
				saldo -= t.getValor();

				break;
			case 2:
				tipoOperacao = "Depósito";
				saldo += t.getValor();
				;
				break;
			case 3:
				tipoOperacao = "Compra Débito";
				saldo -= t.getValor();
				break;
			case 4:
				tipoOperacao = "Compra Crédito";
				limite -= t.getValor();
				break;
			default:
				tipoOperacao = "Não identificado";
				//ControleGeral controle = new ControleGeral();
				//controle.executa("localhost", leaderPath);
				break;
			}

			String infoTransacao = "\r\nTransação: " + t.getCodigo()
					+ " Data: " + t.getData() + "\r\n Tipo: " + t.getOperacao()
					+ "-" + tipoOperacao + "\r\n Valor: " + t.getValor()
					+ "\r\n Descrição: " + t.getDescricao();

			File file = new File(t.getCliente().getCpf() + ".txt");

			FileWriter fileWritter = new FileWriter(file.getName(), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(infoTransacao);
			bufferWritter.close();

			t.getCliente().setConta(new Conta(agencia, numero, saldo, limite));

			zk.setData(caminho,
					t.getCliente().getConta().toString().getBytes(), -1);
			
			this.replicarTransacao(leaderPath, t.getCliente().getConta().toString(), t.getCliente());
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ControleGeral controle = new ControleGeral();
			controle.executa("localhost", leaderPath);
		} 
	}

	public void replicarTransacao(String serverAtivo, String dados, Cliente c) {
		for (int i = 0; i < 5; i++) {
			try {
				conexao = new ZooKeeperConnection();
				zk = conexao.connect("localhost");
				if (Integer.parseInt(serverAtivo.substring(7)) != i) {
					String editPath = serverAtivo.substring(0, 7) + i + "/Clientes/" + c.getCpf() + "/conta";
					zk.setData(editPath,dados.getBytes(), -1);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
