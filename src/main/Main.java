package main;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import Conexao.ZooKeeperConnection;
import SyncPrimitive.LeaderElection;

public class Main {

	/*
	 * Ideia Leader Election - Ao rodar o prog, as infos são guardadas em znodes
	 * diferentes, mas só 1 é o líder definindo a leitura e escrita de infos e
	 * sendo responsavel por replicar as infos para os outros znodes. se este
	 * znode "morre" um novo deve assumir a responsabilidade de leitura, escrita
	 * e replicacao
	 */
	public static void main(String[] args) {

		String address = "localhost";
		ZooKeeperConnection zkConnect = new ZooKeeperConnection();
		ZooKeeper zk;
		try {
			zk = zkConnect.connect(address);
			for (int i = 0; i < 7; i++) {
				Stat s1 = zk.exists("/Server" + i, false);
				if (s1 == null) {
					zk.create("/Server" + i, new Integer(i).toString()
							.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
					zk.create("/Server" + i + "/Clientes", new byte[0],
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
			Stat s2 = zk.exists("/Replicacao",  false);
			if(s2 == null){
				zk.create("/Replicacao" , new byte[0],Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LeaderElection leader = new LeaderElection(address);
		leader.leaderElection(address);

	}

}
