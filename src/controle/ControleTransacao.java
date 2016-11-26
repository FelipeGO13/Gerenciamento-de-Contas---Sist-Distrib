package controle;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import Conexao.ZooKeeperConnection;
import cliente.Cliente;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import transacao.Transacao;

public class ControleTransacao {
private ZooKeeper zk;
	
	private ZooKeeperConnection conexao;
	
	 // Method to create znode in zookeeper ensemble
	private void create(String path, byte[] data) throws 
	      KeeperException,InterruptedException {
	      zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
	      CreateMode.PERSISTENT);
	}
	
	public Transacao criarTransacao(Cliente cliente, int operacao, Double valor){
		Transacao t = new Transacao(cliente,operacao,valor);
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
	         System.out.println("Transação adicionada com sucesso");
	      } catch (Exception e) {
	         System.out.println(e.getMessage()); //Catch error message
	      }
	}	

}
