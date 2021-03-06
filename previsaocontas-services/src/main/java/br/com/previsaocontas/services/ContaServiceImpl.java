package br.com.previsaocontas.services;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.previsaocontas.dao.ContaDAOImpl;
import br.com.previsaocontas.dao.UsuarioDAOImpl;
import br.com.previsaocontas.enums.EnumStatusConta;
import br.com.previsaocontas.enums.EnumTipoConta;
import br.com.previsaocontas.exception.DadosInvalidosException;
import br.com.previsaocontas.model.Conta;
import br.com.previsaocontas.model.Usuario;
import br.com.previsaocontas.utilitarios.UtilObjeto;

@Service
@Transactional(rollbackFor = Throwable.class)
public class ContaServiceImpl {

	@Autowired
	private ContaDAOImpl contaDAOImpl;

	@Autowired
	private UsuarioDAOImpl usuarioDAOImpl;

	public void salvar(Conta contaPai) throws Exception {

		if (contaPai.getId() != null) {
			if (contaPai.getStatus().equals(EnumStatusConta.B)) {
				throw new DadosInvalidosException("Conta já Baixada, verifique!");
			}
		}

		if (contaPai.getValor() == 0) {
			throw new DadosInvalidosException("Informar um Valor para a Conta!");
		}

		if (contaPai.getValor() < somaParciais(contaPai)) {
			throw new DadosInvalidosException("Valor da Conta inferior ao total das parcelas, verifique!!");
		}

		if (UtilObjeto.isNull(contaPai.getQtde_parcelas())) {
			contaPai.setQtde_parcelas(1);
		}

		if (contaPai.getContasFilhas() != null) {
			for (Conta conta1 : contaPai.getContasFilhas()) {
				conta1.setContaPai(contaPai);
				conta1.setUsuario(contaPai.getUsuario());
			}
		}

		// Altera Valor da conta para o valor das parciais (caso
		// parciais maior que conta)
		Double totalParciais = somaParciais(contaPai, false);
		if ((totalParciais) > contaPai.getValor()) {
			contaPai.setValor(totalParciais);
		}

		if (contaPai.getId() == null) {

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(contaPai.getData_mes());

			Long numrAgrupador = contaDAOImpl.obterProximo(contaPai.getUsuario());
			for (int i = 0; i < contaPai.getQtde_parcelas(); i++) {
				Conta contac = SerializationUtils.clone(contaPai);
				if (contac.getQtde_parcelas() > 1) {
					contac.setId(null);
				}
				contac.setNumr_agrupador(numrAgrupador);
				contac.setNumr_parcela(i + 1);
				if (i > 0) {
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					calendar.add(Calendar.MONTH, 1);
				}
				contac.setData_mes(calendar.getTime());
				contaDAOImpl.salvar(contac);

			}
		} else {

			contaDAOImpl.salvar(contaPai);
		}

	}

	public Double somaParciais(Conta conta) {
		return somaParciais(conta, true);
	}

	public Double somaParciais(Conta conta, boolean apenasBaixadas) {

		if (conta == null) {
			return 0.0;
		}
		if (conta.getContasFilhas() == null) {
			return 0.0;
		}

		Double totalParciais = 0.0;
		for (Conta contaFilha : conta.getContasFilhas()) {
			if (apenasBaixadas) {
				if (contaFilha.getStatus().equals(EnumStatusConta.B)) {
					totalParciais += contaFilha.getValor();
				}
			} else {
				totalParciais += contaFilha.getValor();
			}
		}
		return totalParciais;

	}

	public Double somaParciais(List<Conta> contasFilhas) {

		Double totalParciais = 0.0;
		for (Conta contaFilha : contasFilhas) {
			if (contaFilha.getStatus().equals(EnumStatusConta.B)) {
				totalParciais += contaFilha.getValor();
			}
		}
		return totalParciais;

	}

	public void exluir(Conta conta) throws Exception {
		if (conta.getStatus().equals(EnumStatusConta.B)) {
			throw new DadosInvalidosException("Conta já baixada, verifique!");
		}

		if (conta.getContasFilhas() != null) {
			for (Conta conta1 : conta.getContasFilhas()) {
				if (conta1.getStatus().equals(EnumStatusConta.B)) {
					throw new DadosInvalidosException("Existem parciais pagas, favor cancelar o pagamento!");
				}
			}
		}

		Double totalParciais = somaParciais(conta);
		if (totalParciais > 0) {
			if (conta.getTipo().equals(EnumTipoConta.D)) {
				conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() + totalParciais);
			}
			if (conta.getTipo().equals(EnumTipoConta.R)) {
				conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() - totalParciais);
			}
			usuarioDAOImpl.salvar(conta.getUsuario());
		}
		contaDAOImpl.remover(conta);
	}

	public List<Conta> buscaContaMes(Integer mesSelecionado, Integer anoSelecionado, Usuario usuario) throws Exception {
		return contaDAOImpl.buscaContaMes(mesSelecionado, anoSelecionado, usuario);

	}

	public List<Conta> buscaContasAcumuladoAberto(Integer mesSelecionado, Integer anoSelecionado, Usuario usuario)
			throws Exception {
		return contaDAOImpl.buscaContasAcumuladoAberto(mesSelecionado, anoSelecionado, usuario);
	}

	public void baixarConta(Conta conta) throws Exception {
		if (conta.getStatus().equals(EnumStatusConta.A)) {
			conta.setStatus(EnumStatusConta.B);

			if (conta.getContaPai() == null) {
				// Atualiza Saldo
				Double totalParciais = somaParciais(conta);
				if (totalParciais != conta.getValor()) {
					if (conta.getTipo().equals(EnumTipoConta.D)) {
						conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() - (conta.getValor() - totalParciais));
					}
					if (conta.getTipo().equals(EnumTipoConta.R)) {
						conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() + (conta.getValor() - totalParciais));
					}
				}

				contaDAOImpl.salvar(conta);

			} else {

				// Conta Filha
				if (conta.getContaPai().getStatus().equals(EnumStatusConta.B)) {
					throw new DadosInvalidosException("Conta Pai já baixada, verifique!");
				}

				if (conta.getContaPai().getTipo().equals(EnumTipoConta.D)) {
					conta.getContaPai().getUsuario()
							.setSaldo(conta.getContaPai().getUsuario().getSaldo() - (conta.getValor()));
				}
				if (conta.getContaPai().getTipo().equals(EnumTipoConta.R)) {
					conta.getContaPai().getUsuario()
							.setSaldo(conta.getContaPai().getUsuario().getSaldo() + (conta.getValor()));
				}

				contaDAOImpl.salvar(conta.getContaPai());

			}

		}
	}

	public void abrirConta(Conta conta) throws Exception {

		if (conta.getStatus().equals(EnumStatusConta.B)) {
			conta.setStatus(EnumStatusConta.A);

			if (conta.getContaPai() == null) {
				// Atualiza Saldo
				Double totalParciais = somaParciais(conta);
				if (totalParciais != conta.getValor()) {
					if (conta.getTipo().equals(EnumTipoConta.D)) {
						conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() + (conta.getValor() - totalParciais));
					}
					if (conta.getTipo().equals(EnumTipoConta.R)) {
						conta.getUsuario().setSaldo(conta.getUsuario().getSaldo() - (conta.getValor() - totalParciais));
					}
				}

				contaDAOImpl.salvar(conta);

			} else {

				// Conta Filha

				if (conta.getContaPai().getStatus().equals(EnumStatusConta.B)) {
					throw new DadosInvalidosException("Conta Pai já baixada, verifique!");
				}
				if (conta.getContaPai().getTipo().equals(EnumTipoConta.D)) {
					conta.getContaPai().getUsuario()
							.setSaldo(conta.getContaPai().getUsuario().getSaldo() + (conta.getValor()));
				}
				if (conta.getContaPai().getTipo().equals(EnumTipoConta.R)) {
					conta.getContaPai().getUsuario()
							.setSaldo(conta.getContaPai().getUsuario().getSaldo() - (conta.getValor()));
				}

				contaDAOImpl.salvar(conta.getContaPai());

			}

		}

	}

	public List<Conta> obterPorAtributo(String string, Long numr_agrupador) throws Exception {
		return (List<Conta>) contaDAOImpl.obterPorAtributo(string, numr_agrupador);

	}

	public Conta obter(Long id) throws Exception {
		return contaDAOImpl.obter(id);
	}

	public void salvarTodos(Conta conta) throws Exception {
		List<Conta> contas = (List<Conta>) contaDAOImpl.obterPorAtributo("numr_agrupador", conta.getNumr_agrupador());

		for (Conta conta2 : contas) {
			if (conta2.getStatus().equals(EnumStatusConta.B) || somaParciais(conta) > 0) {
				continue;
			}
			conta2.setDescricao(conta.getDescricao());
			conta2.setDia_vencimento(conta.getDia_vencimento());
			conta2.setFlag_comum(conta.getFlag_comum());
			conta2.setValor(conta.getValor());
			contaDAOImpl.salvar(conta2);
		}

	}

	public void validaContaFilha(Conta conta, Conta parcial) throws DadosInvalidosException {

		if (conta.getStatus().equals(EnumStatusConta.B)) {
			throw new DadosInvalidosException("Conta Baixada, verifique!");
		}

		if (parcial.getValor() == 0) {
			throw new DadosInvalidosException("Valor Zerado, verifique!");
		}

	}

}
