package controle;

import java.util.Date;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import bean.Cliente;
import bean.Transacao;

public class ControleTransacao {
private ZooKeeper zk;
	
	private ZooKeeperConnection conexao;
	
	 // Method to create znode in zookeeper ensemble
	private void create(String path, byte[] data) throws 
	      KeeperException,InterruptedException {
	      zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
	      CreateMode.PERSISTENT);
	}
	
	public Transacao criarTransacao(Integer codigo, String descricao, Integer operacao, Double valor, Cliente cliente, Date data){
		Transacao t = new Transacao(codigo, descricao, operacao, valor, cliente, data);
		znodeTransacao(cliente,t);
		return t;
	}
	
	public void znodeTransacao(Cliente c, Transacao t){	
			
		String caminho = "/Clientes/"+c.getCpf()+"/transacoes";		
		String dados = t.getOperacao()+","+t.getValor();
		
		 try {
	         conexao = new ZooKeeperConnection();
	         zk = conexao.connect("localhost");
	         
	         
	         create(caminho, dados.getBytes()); // Create the data to the specified path
	         conexao.close();
	         System.out.println("Transa��o adicionada com sucesso");
	      } catch (Exception e) {
	         System.out.println(e.getMessage()); //Catch error message
	      }
	}	

}
