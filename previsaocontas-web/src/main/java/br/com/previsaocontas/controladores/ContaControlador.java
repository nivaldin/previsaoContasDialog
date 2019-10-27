package br.com.previsaocontas.controladores;

import br.com.previsaocontas.enums.EnumSimNao;
import br.com.previsaocontas.enums.EnumStatusConta;
import br.com.previsaocontas.enums.EnumTipoConta;
import br.com.previsaocontas.exception.DadosInvalidosException;
import br.com.previsaocontas.model.Conta;
import br.com.previsaocontas.model.Usuario;
import br.com.previsaocontas.services.ContaServiceImpl;
import br.com.previsaocontas.services.UsuarioServiceImpl;
import br.com.previsaocontas.utilitarios.UtilNumero;
import br.com.previsaocontas.utilitarios.UtilObjeto;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

@ManagedBean
@SessionScoped
public class ContaControlador implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{contaServiceImpl}")
	private ContaServiceImpl contaServiceImpl;

	@ManagedProperty(value = "#{usuarioServiceImpl}")
	private UsuarioServiceImpl usuarioServiceImpl;

	private Conta conta;

	private List<Conta> contas;
	private List<Conta> contasDespesa;
	private List<Conta> contasReceita;

	private Usuario usuario;

	private Integer numr_parcelas = 0;

	private Conta contaFilha;

	private List<Conta> parcelas;

	private Integer mesSelecionado;

	private Integer anoSelecionado;

	private Double totalDespesaMes = 0.0;
	private Double totalReceitaMes = 0.0;

	private Double totalDespesaPrevistoMes = 0.0;
	private Double totalReceitaPrevistoMes = 0.0;

	private Double totalDespesaPrevistoAcumulado = 0.0;
	private Double totalReceitaPrevistoAcumulado = 0.0;

	private Integer indexTabMes = 0;

	private PieChartModel modelChart = new PieChartModel();
	private PieChartModel modelChartAcumulado = new PieChartModel();

	public Conta getContaFilha() {
		return contaFilha;
	}

	public void setContaFilha(Conta contaFilha) {
		this.contaFilha = contaFilha;
	}

	public ContaServiceImpl getContaServiceImpl() {
		return contaServiceImpl;
	}

	public void setContaServiceImpl(ContaServiceImpl contaServiceImpl) {
		this.contaServiceImpl = contaServiceImpl;
	}

	public UsuarioServiceImpl getUsuarioServiceImpl() {
		return usuarioServiceImpl;
	}

	public void setUsuarioServiceImpl(UsuarioServiceImpl usuarioServiceImpl) {
		this.usuarioServiceImpl = usuarioServiceImpl;
	}

	private void dadosChartAcumulado() {
		PieChartModel pieModel = new PieChartModel();
		ChartData data = new ChartData();

		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();
		values.add(totalDespesaPrevistoAcumulado);
		values.add(totalReceitaPrevistoAcumulado);
		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("red");
		bgColors.add("green");
		dataSet.setBackgroundColor(bgColors);

		data.addChartDataSet(dataSet);
		List<String> labels = new ArrayList<>();
		labels.add("Despesa Acum.");
		labels.add("Receita Acum.");
		data.setLabels(labels);

		pieModel.setData(data);
		setModelChartAcumulado(pieModel);
	}
	private void dadosChartMes() {
		PieChartModel pieModel = new PieChartModel();
		ChartData data = new ChartData();

		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();
		values.add(totalDespesaMes);
		values.add(totalReceitaMes);
		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("red");
		bgColors.add("green");
		dataSet.setBackgroundColor(bgColors);

		data.addChartDataSet(dataSet);
		List<String> labels = new ArrayList<>();
		labels.add("Despesa Mês");
		labels.add("Receita Mês");
		data.setLabels(labels);

		pieModel.setData(data);
		modelChart = pieModel;
	}

	public List<Conta> getContasDespesa() {
		if (this.contasDespesa == null) {
			this.setContasDespesa(new ArrayList<Conta>());
		}
		return contasDespesa;
	}

	public void setContasDespesa(List<Conta> contasDespesa) {
		this.contasDespesa = contasDespesa;
	}

	public List<Conta> getContasReceita() {
		if (this.contasReceita == null) {
			this.setContasReceita(new ArrayList<Conta>());
		}
		return contasReceita;
	}

	public void setContasReceita(List<Conta> contasReceita) {
		this.contasReceita = contasReceita;
	}

	public Integer getIndexTabMes() {
		return indexTabMes;
	}

	public void setIndexTabMes(Integer indexTabMes) {
		this.indexTabMes = indexTabMes;
	}

	public Conta getConta() {
		if (this.conta == null) {
			this.setConta(new Conta());
		}
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	public List<Conta> getContas() {
		if (this.contas == null) {
			this.setContas(new ArrayList<Conta>());
		}
		return contas;
	}

	public void setContas(List<Conta> contas) {
		this.contas = contas;
	}

	public Integer getMesSelecionado() {
		return mesSelecionado;
	}

	public void setMesSelecionado(Integer mesSelecionado) {
		this.mesSelecionado = mesSelecionado;
	}

	public Integer getAnoSelecionado() {
		return anoSelecionado;
	}

	public void setAnoSelecionado(Integer anoSelecionado) {
		this.anoSelecionado = anoSelecionado;
	}

	public Double getTotalDespesaMes() {
		return totalDespesaMes;
	}

	public void setTotalDespesaMes(Double totalDespesaMes) {
		this.totalDespesaMes = totalDespesaMes;
	}

	public Double getTotalReceitaMes() {
		return totalReceitaMes;
	}

	public void setTotalReceitaMes(Double totalReceitaMes) {
		this.totalReceitaMes = totalReceitaMes;
	}

	public Double getTotalDespesaPrevistoMes() {
		return totalDespesaPrevistoMes;
	}

	public void setTotalDespesaPrevistoMes(Double totalDespesaPrevistoMes) {
		this.totalDespesaPrevistoMes = totalDespesaPrevistoMes;
	}

	public Double getTotalReceitaPrevistoMes() {
		return totalReceitaPrevistoMes;
	}

	public void setTotalReceitaPrevistoMes(Double totalReceitaPrevistoMes) {
		this.totalReceitaPrevistoMes = totalReceitaPrevistoMes;
	}

	public Double getTotalDespesaPrevistoAcumulado() {
		return totalDespesaPrevistoAcumulado;
	}

	public void setTotalDespesaPrevistoAcumulado(Double totalDespesaPrevistoAcumulado) {
		this.totalDespesaPrevistoAcumulado = totalDespesaPrevistoAcumulado;
	}

	public Double getTotalReceitaPrevistoAcumulado() {
		return totalReceitaPrevistoAcumulado;
	}

	public void setTotalReceitaPrevistoAcumulado(Double totalReceitaPrevistoAcumulado) {
		this.totalReceitaPrevistoAcumulado = totalReceitaPrevistoAcumulado;
	}

	public Usuario getUsuario() {
		if (this.usuario == null || this.usuario.getId() == null) {
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
					.getSession(false);
			Usuario usuario = (Usuario) session.getAttribute("usuario");
			if (usuario == null) {
				usuario = new Usuario();
			} else {
				try {
					usuario = usuarioServiceImpl.obter(usuario.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.setUsuario(usuario);
		}
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Conta> getParcelas() {
		if (this.parcelas == null) {
			this.setParcelas(new ArrayList<Conta>());
		}
		return parcelas;
	}

	public void setParcelas(List<Conta> parcelas) {
		this.parcelas = parcelas;
	}

	public Integer getNumr_parcelas() {
		return numr_parcelas;
	}

	public void setNumr_parcelas(Integer numr_parcelas) {
		this.numr_parcelas = numr_parcelas;
	}

	public String validaEntrada() {
		try {

			if (this.getUsuario().getId() != null) {
				return this.abreInicial();
			}
		} catch (Exception e) {

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}
		return null;

	}

	public void atualizaSaldo() {

		try {

			usuarioServiceImpl.salvar(this.getUsuario());

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo com Sucesso!", null));

		} catch (Exception e) {

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void atualizaInicio() {
		try {
			this.buscaContasMes();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void excluir() {
		try {

			Conta contaExclusao = this.contaServiceImpl.obter(this.getConta().getId());
			this.contaServiceImpl.exluir(contaExclusao);
			this.atualizaInicio();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Excluído com Sucesso!", null));
		} catch (DadosInvalidosException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage().toString(), null));
			e.printStackTrace();
		} catch (Exception e) {
			try {
				this.buscaContasMes();
			} catch (Exception ex) {
				e.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void buscaContasMes() throws Exception {

		this.iniciaMesAno();

		this.setIndexTabMes(this.mesSelecionado - 1);

		this.setContas(contaServiceImpl.buscaContaMes(mesSelecionado, anoSelecionado, this.getUsuario()));

		this.calculaResumoMes();

		this.buscaUsuario();

	}

	private void iniciaMesAno() {

		if (UtilObjeto.isNull(mesSelecionado) || UtilObjeto.isNull(anoSelecionado)) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);
			this.mesSelecionado = month + 1;
			this.anoSelecionado = year;

		}
	}

	private void calculaResumoMes() throws Exception {

		this.setTotalDespesaMes(0.0);
		this.setTotalReceitaMes(0.0);
		this.setTotalDespesaPrevistoMes(0.0);
		this.setTotalReceitaPrevistoMes(0.0);
		this.setTotalDespesaPrevistoAcumulado(0.0);
		this.setTotalReceitaPrevistoAcumulado(0.0);

		for (Conta conta : this.getContas()) {

			// Despesas Mes
			if (conta.getTipo().equals(EnumTipoConta.D)) {

				this.totalDespesaMes += conta.getValor();

				if (conta.getStatus().equals(EnumStatusConta.A)) {

					Double totalParciais = somaParciais(conta);
					if (totalParciais == conta.getValor()) {

						totalParciais = 0.0;

					}
					this.totalDespesaPrevistoMes += conta.getValor() - UtilNumero.round(totalParciais, 2);

				}

			}
			// Receitas Mes
			if (conta.getTipo().equals(EnumTipoConta.R)) {

				this.totalReceitaMes += conta.getValor();

				if (conta.getStatus().equals(EnumStatusConta.A)) {

					Double totalParciais = somaParciais(conta);
					if (totalParciais == conta.getValor()) {

						totalParciais = 0.0;

					}
					this.totalReceitaPrevistoMes += conta.getValor() - UtilNumero.round(totalParciais, 2);

				}
			}
		}

		List<Conta> contas = contaServiceImpl.buscaContasAcumuladoAberto(mesSelecionado, anoSelecionado,
				this.getUsuario());
		for (Conta conta : contas) {

			if (conta.getTipo().equals(EnumTipoConta.D)) {

				Double totalParciais = somaParciais(conta);
				if (totalParciais == conta.getValor()) {

					totalParciais = 0.0;

				}
				this.totalDespesaPrevistoAcumulado += conta.getValor() - UtilNumero.round(totalParciais, 2);

			}

			if (conta.getTipo().equals(EnumTipoConta.R)) {

				Double totalParciais = somaParciais(conta);
				if (totalParciais == conta.getValor()) {

					totalParciais = 0.0;

				}
				this.totalReceitaPrevistoAcumulado += conta.getValor() - UtilNumero.round(totalParciais, 2);

			}
		}
		this.dadosChartMes();
		this.dadosChartAcumulado();
	}

	public void baixarContaFilha() {

		try {

			this.contaServiceImpl.baixarConta(contaServiceImpl.obter(this.getContaFilha().getId()));
			this.preparaEdicao();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Conta Baixada!", null));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			this.preparaEdicao();
			e.printStackTrace();
		}

	}

	public void abrirContaFilha() {

		try {

			this.contaServiceImpl.abrirConta(this.getContaFilha());
			this.preparaEdicao();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Conta Aberta!", null));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			this.preparaEdicao();
			e.printStackTrace();
		}

	}

	public void baixarConta() {

		try {

			this.contaServiceImpl.baixarConta(this.getConta());

			this.buscaContasMes();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Conta Baixada!", null));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			this.preparaEdicao();
			e.printStackTrace();
		}

	}

	public void abrirConta() {

		try {

			this.contaServiceImpl.abrirConta(this.getConta());
			this.buscaContasMes();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Conta Aberta!", null));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			this.preparaEdicao();
			e.printStackTrace();
		}

	}

	public void mesProximo() throws Exception {

		this.mesSelecionado += 1;
		if (this.mesSelecionado > 12) {
			this.mesSelecionado = 1;
			this.anoSelecionado += 1;
		}
		this.buscaContasMes();

	}

	public void mesAnterior() throws Exception {

		this.mesSelecionado -= 1;
		if (this.mesSelecionado == 0) {
			this.mesSelecionado = 12;
			this.anoSelecionado -= 1;
		}
		this.buscaContasMes();

	}

	public String abreLogin() {
		this.getUsuario();
		return "/login.xhtml?faces-redirect=true";
	}

	public String abreInicial() {

		this.atualizaInicio();
		return "/contas/inicial.xhtml?faces-redirect=true";
	}

	public String abreIncluir() {

		this.preparaInclusao();
		return "/contas/incluir.xhtml?faces-redirect=true";

	}

	public String abreAlterar() {

		this.preparaEdicao();
		return "/contas/incluir.xhtml?faces-redirect=true";

	}

	private void buscaUsuario() {
		try {
			this.setUsuario(usuarioServiceImpl.obter(this.getUsuario().getId()));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void salvar() {

		try {

			this.getConta().setUsuario(this.getUsuario());
			this.getContaServiceImpl().salvar(this.getConta());
			this.preparaInclusao();
			this.buscaContasMes();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo com Sucesso!", null));

		} catch (DadosInvalidosException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage().toString(), null));
			e.printStackTrace();

		} catch (Exception e) {
			try {
				this.buscaContasMes();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void salvarTodos() {

		try {

			this.contaServiceImpl.salvarTodos(this.getConta());

			this.preparaInclusao();
			this.buscaContasMes();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo Todos com Sucesso!", null));
		} catch (DadosInvalidosException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage().toString(), null));
			e.printStackTrace();
		} catch (Exception e) {
			try {
				this.buscaContasMes();
			} catch (Exception ex) {
				e.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}

	}

	public void excluirTodos() {

		try {

			List<Conta> contas = (List<Conta>) contaServiceImpl.obterPorAtributo("numr_agrupador",
					this.getConta().getNumr_agrupador());

			for (Conta conta : contas) {
				if (conta.getStatus().equals(EnumStatusConta.B) || somaParciais(conta) > 0) {
					continue;
				}
				contaServiceImpl.exluir(conta);

			}

			this.preparaInclusao();

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Excluido Todos com Sucesso!", null));
		} catch (DadosInvalidosException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage().toString(), null));
			e.printStackTrace();
		} catch (Exception e) {
			try {
				this.buscaContasMes();
			} catch (Exception ex) {
				e.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
			e.printStackTrace();
		}
	}

	public List<EnumTipoConta> getListaTipo() {

		return Arrays.asList(EnumTipoConta.values());

	}

	public List<EnumStatusConta> getListaStatus() {

		return Arrays.asList(EnumStatusConta.A, EnumStatusConta.B);

	}

	public List<EnumSimNao> getListaFlagComum() {

		return Arrays.asList(EnumSimNao.values());

	}

	public void preparaInclusao() {

		this.setContaFilha(new Conta());
		this.parcelas = new ArrayList<Conta>();

		this.setConta(new Conta());

		this.iniciaMesAno();

		Calendar calendar = Calendar.getInstance();
		if (this.mesSelecionado - 1 != calendar.get(Calendar.MONTH)) {
			calendar.set(Calendar.DAY_OF_MONTH, 1);
		}
		calendar.set(Calendar.MONTH, this.mesSelecionado - 1);
		calendar.set(Calendar.YEAR, this.anoSelecionado);

		this.getConta().setData_mes(calendar.getTime());

		this.getConta().setFlag_comum(EnumSimNao.N);
		this.numr_parcelas = 0;

	}

	public void preparaEdicao() {
		try {

			this.parcelas = new ArrayList<Conta>();

			this.setConta(contaServiceImpl.obter(this.getConta().getId()));

			Collections.sort(this.getConta().getContasFilhas(), new Comparator<Conta>() {

				public int compare(Conta parcial1, Conta parcial2) {

					return parcial2.getData_registro().compareTo(parcial1.getData_registro());
				}
			});

			this.novaParcial();

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
		}

	}

	private void novaParcial() {

		this.setContaFilha(new Conta());
		this.getContaFilha().setData_registro(new Date());
		this.getContaFilha().setData_mes(this.getConta().getData_mes());
		this.getContaFilha().setFlag_comum(this.getConta().getFlag_comum());
		this.getContaFilha().setTipo(this.getConta().getTipo());
		this.getContaFilha().setStatus(EnumStatusConta.A);

	}

	public Double somaParciais(Conta conta) {

		return this.getContaServiceImpl().somaParciais(conta);

	}

	public void incluirParcial() {

		try {

			this.contaServiceImpl.validaContaFilha(this.getConta(), this.getContaFilha());

			this.getConta().getContasFilhas().add(this.getContaFilha());

			this.getContaServiceImpl().salvar(this.getConta());

			this.preparaEdicao();

			Collections.sort(this.getConta().getContasFilhas(), new Comparator<Conta>() {

				public int compare(Conta parcial1, Conta parcial2) {

					return parcial2.getData_registro().compareTo(parcial1.getData_registro());
				}
			});

		} catch (DadosInvalidosException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage().toString(), null));
		} catch (Exception e) {
			this.getConta().getContasFilhas().remove(this.getContaFilha());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
		}

	}

	public void removerParcial() {

		try {

			if (this.getConta().getStatus().equals(EnumStatusConta.B)) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Conta Baixada, verifique!", null));
				return;
			}

			if (this.getContaFilha().getStatus().equals(EnumStatusConta.B)) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Conta Filha Baixada, verifique!", null));
				return;
			}

			this.getConta().getContasFilhas().remove(this.getContaFilha());
			this.getContaServiceImpl().salvar(this.getConta());
			
//			Conta contaExclusao = this.getContaServiceImpl().obter(this.getContaFilha().getId());
//			this.getContaServiceImpl().exluir(contaExclusao);
			 
			this.preparaEdicao();

		} catch (Exception e) {
			this.getConta().getContasFilhas().add(this.getContaFilha());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage().toString(), null));
		}

	}

	public String contaStyleClass(Conta conta) throws Exception {

		String styleClass = "";

		if (conta.getTipo().equals(EnumTipoConta.D)) {
			if (conta.getStatus().equals(EnumStatusConta.B)) {
				styleClass = "rowBaixadoD";
			} else {
				styleClass = "rowDespesa";

				if (this.somaParciais(conta) > 0) {
					styleClass = "rowParciaisD";
				}

			}

		}
		if (conta.getTipo().equals(EnumTipoConta.R)) {

			if (conta.getStatus().equals(EnumStatusConta.B)) {
				styleClass = "rowBaixadoR";
			} else {
				styleClass = "rowReceita";

				if (this.somaParciais(conta) > 0) {
					styleClass = "rowParciaisR";
				}

			}

		}

		return styleClass;
	}

	public String verificaVencimento(Conta conta) {

		if (conta == null) {
			return "";
		}
		if (conta.getDia_vencimento() == null) {
			return "";
		}

		if (conta.getStatus().equals(EnumStatusConta.B)) {
			return "";
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, mesSelecionado - 1);
		calendar.set(Calendar.YEAR, anoSelecionado);
		calendar.set(Calendar.DAY_OF_MONTH, conta.getDia_vencimento() - 1);

		String classe = "";
		if (calendar.getTime().before(new Date())) {
			classe = "colVencido";
		}
		return classe;

	}

	public String verificaFlagComum(Conta conta) {

		if (conta == null) {
			return "";
		}

		if (conta.getFlag_comum().equals(EnumSimNao.S)) {
			return "colFlagComum";
		} else {
			return "";
		}
	}

	public String verificaTipoConta(Conta conta) {

		if (conta == null) {
			return "";
		}

		if (conta.getTipo().equals(EnumTipoConta.D)) {
			return "colDespesa";
		} else if (conta.getTipo().equals(EnumTipoConta.R)) {
			return "colReceita";
		} else {
			return "";
		}
	}

	public PieChartModel getModelChart() {
		return modelChart;
	}

	public void setModelChart(PieChartModel modelChart) {
		this.modelChart = modelChart;
	}

	public PieChartModel getModelChartAcumulado() {
		return modelChartAcumulado;
	}

	public void setModelChartAcumulado(PieChartModel modelChartAcumulado) {
		this.modelChartAcumulado = modelChartAcumulado;
	}
}
