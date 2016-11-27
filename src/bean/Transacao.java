package bean;

public class Transacao {

	private Integer codigo;
	private String descricao;
	private Integer operacao; 
	private Double valor;
	private Cliente cliente;
	private String data;
	
	public Transacao() {
		// TODO Auto-generated constructor stub
	}
	
	public Transacao(Integer codigo, String descricao, Integer operacao, Double valor, Cliente cliente, String data) {
		this.codigo = codigo;
		this.descricao = descricao;
		this.operacao = operacao;
		this.valor = valor;
		this.cliente = cliente;
		this.setData(data);
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getOperacao() {
		return operacao;
	}

	public void setOperacao(Integer operacao) {
		this.operacao = operacao;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return codigo+","+cliente.getCpf()+","+operacao+","+valor+","+data+","+descricao;
	}
	
}
